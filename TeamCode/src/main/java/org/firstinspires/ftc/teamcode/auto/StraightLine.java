package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.buttons.BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.driveto.DriveTo;
import org.firstinspires.ftc.teamcode.driveto.DriveToListener;
import org.firstinspires.ftc.teamcode.driveto.DriveToParams;
import org.firstinspires.ftc.teamcode.field.Field;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;
import org.firstinspires.ftc.teamcode.wheels.TankDrive;

import static org.firstinspires.ftc.teamcode.auto.DriveToMethods.driveBackward;
import static org.firstinspires.ftc.teamcode.auto.DriveToMethods.driveForward;

@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Straight Line", group = "Auto")
public class StraightLine extends OpMode implements DriveToListener {

    // Auto constants
    private static final int RELEASE_REVERSE_MM = 50;

    // Devices and subsystems
    private CommonTasks common = null;
    private TankDrive tank = null;
    private ServoFTC clawTop = null;
    private ServoFTC clawBottom = null;
    private Motor lift = null;

    // Runtime state
    private AutoTracker autoTracker = new AutoTracker();
    private AUTO_STATE state = AUTO_STATE.LIFT_INIT;
    private boolean liftReady = false;

    // Init-time config
    private ButtonHandler buttons = new ButtonHandler();
    private Field.AllianceColor alliance = Field.AllianceColor.BLUE;
    private DISTANCE distance = DISTANCE.SHORT;
    private DELAY delay = DELAY.NONE;

    @Override
    public void init() {

        // Placate drivers
        telemetry.addData(">", "Initializing...");
        telemetry.update();

        // Init the common tasks elements
        common = new CommonTasks(hardwareMap, telemetry);
        common.initDrive(tank);
        common.initLift(lift);
        common.initClaws(clawTop, clawBottom);

        // Register buttons
        buttons.register("DELAY-UP", gamepad1, BUTTON.dpad_up);
        buttons.register("DELAY-DOWN", gamepad1, BUTTON.dpad_down);
        buttons.register("DISTANCE-UP", gamepad1, BUTTON.dpad_right);
        buttons.register("DISTANCE-DOWN", gamepad1, BUTTON.dpad_left);
        buttons.register("ALLIANCE-RED", gamepad1, BUTTON.b);
        buttons.register("ALLIANCE-BLUE", gamepad1, BUTTON.x);

        // Wait for the game to begin
        telemetry.addData(">", "Init complete");
        telemetry.update();
    }

    @Override
    public void init_loop() {

        // Zero the lift
        if (!liftReady) {
            // TODO: We need to zero the lift, for now just pretend
            liftReady = true;
        }

        // Update the dpad buttons
        buttons.update();

        // Adjust delay
        if (buttons.get("DELAY-UP")) {
            delay = delay.next();
        } else if (buttons.get("DELAY-DOWN")) {
            delay = delay.prev();
        }

        // Adjust distance
        if (buttons.get("DISTANCE-UP")) {
            distance = distance.next();
        } else if (buttons.get("DISTANCE-DOWN")) {
            distance = distance.prev();
        }

        // Adjust alliance color
        if (buttons.get("ALLIANCE-RED")) {
            alliance = Field.AllianceColor.RED;
        } else if (buttons.get("ALLIANCE-BLUE")) {
            alliance = Field.AllianceColor.BLUE;
        }

        // Driver feedback
        telemetry.addData("Delay", delay);
        telemetry.addData("Distance", distance);
        telemetry.addData("Alliance", alliance);
        telemetry.addData("Lift", liftReady ? "Ready" : "Zeroing");
        telemetry.update();
    }

    @Override
    public void start() {
        telemetry.clearAll();

        // Disable the lift if it isn't ready
        lift.setEnabled(liftReady);

        // Steady...
        state = AUTO_STATE.values()[0];
    }

    @Override
    public void loop() {
        // Convert intervals to times each loop
        autoTracker.setTimer(time);

        // Handle DriveTo driving
        if (autoTracker.drive != null) {
            // DriveTo
            autoTracker.drive.drive();

            // Return to teleop when complete
            if (autoTracker.drive.isDone()) {
                autoTracker.drive = null;
                tank.setTeleop(true);
            }
        }

        // Driver feedback
        telemetry.addData("State", state);
        telemetry.addData("Encoder", tank.getEncoder());
        telemetry.update();

        /*
         * Cut the loop short when we are auto-driving or waiting on a timer
         * This keeps us out of the state machine until the preceding auto-drive command is complete
         */
        if (autoTracker.drive != null || autoTracker.timer > time) {
            return;
        }

        // Main state machine
        switch (state) {
            case INIT:
                autoTracker.done = false;
                state = state.next();
                break;
            case LIFT_INIT:
                // Delegate control to liftAutoStart
                autoTracker = common.liftAutoStart(lift, clawTop, clawBottom);

                // Stay in LIFT_INIT until all the liftAutoStart operations are complete
                if (autoTracker.done) {
                    state = state.next();
                    autoTracker.done = false;
                }
                break;
            case DELAY:
                autoTracker.timer = time + delay.seconds();
                state = state.next();
                break;
            case DRIVE_FORWARD:
                autoTracker.drive = driveForward(this, tank, distance.millimeters());
                state = state.next();
                break;
            case RELEASE:
                clawTop.min();
                clawBottom.min();
                state = state.next();
                break;
            case RELEASE_DELAY:
                autoTracker.timer = time + 1;
                state = state.next();
                break;
            case RELEASE_REVERSE:
                autoTracker.drive = driveBackward(this, tank, RELEASE_REVERSE_MM);
                state = state.next();
                break;
            case DONE:
                // Exit the opmode
                this.requestOpModeStop();
                break;
        }
    }

    @Override
    public void driveToStop(DriveToParams param) {
        DriveToMethods.stop(tank, param);
    }

    @Override
    public void driveToRun(DriveToParams param) {
        DriveToMethods.run(tank, param);
    }

    @Override
    public double driveToSensor(DriveToParams param) {
        return DriveToMethods.sensor(tank, param);
    }

    // Define the order of auto routine components
    enum AUTO_STATE implements OrderedEnum {
        INIT,
        LIFT_INIT,
        DELAY,
        DRIVE_FORWARD,
        RELEASE,
        RELEASE_DELAY,
        RELEASE_REVERSE,
        DONE;

        public AUTO_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public AUTO_STATE next() {
            return OrderedEnumHelper.next(this);
        }
    }

    // Configurable straight-line distance
    enum DISTANCE implements OrderedEnum {
        SHORT(965),
        LONG(1016);

        private int millimeters;

        DISTANCE(int millimeters) {
            this.millimeters = millimeters;
        }

        public int millimeters() {
            return millimeters;
        }

        public DISTANCE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public DISTANCE next() {
            return OrderedEnumHelper.next(this);
        }
    }

    // Configurable delay
    enum DELAY implements OrderedEnum {
        NONE(0),
        SHORT(5000),
        LONG(10000);

        private int milliseconds;

        DELAY(int milliseconds) {
            this.milliseconds = milliseconds;
        }

        public int milliseconds() {
            return milliseconds;
        }

        public double seconds() {
            return milliseconds / 1000.0d;
        }

        public DELAY prev() {
            return OrderedEnumHelper.prev(this);
        }

        public DELAY next() {
            return OrderedEnumHelper.next(this);
        }
    }
}