package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.buttons.SinglePressButton;
import org.firstinspires.ftc.teamcode.config.ServoConfigs;
import org.firstinspires.ftc.teamcode.config.MotorConfigs;
import org.firstinspires.ftc.teamcode.config.WheelMotorConfigs;
import org.firstinspires.ftc.teamcode.driveto.DriveTo;
import org.firstinspires.ftc.teamcode.driveto.DriveToListener;
import org.firstinspires.ftc.teamcode.driveto.DriveToParams;
import org.firstinspires.ftc.teamcode.field.Field;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;
import org.firstinspires.ftc.teamcode.wheels.TankDrive;

import static org.firstinspires.ftc.teamcode.auto.DriveToMethods.*;

@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Straight Line", group = "Auto")
public class StraightLine extends OpMode implements DriveToListener {

    // Auto constants
    private static final double LIFT_DELAY = 0.75;

    // Devices and subsystems
    private TankDrive tank = null;
    private DriveTo drive = null;
    private ServoFTC clawTop = null;
    private ServoFTC clawBottom = null;
    private Motor lift = null;

    // Runtime state
    private AUTO_STATE state = AUTO_STATE.INIT;
    private double timer = 0;
    private boolean liftReady = false;

    // Init-time config
    // TODO: Add a handler to register and update all these at once
    private SinglePressButton up = new SinglePressButton();
    private SinglePressButton down = new SinglePressButton();
    private SinglePressButton left = new SinglePressButton();
    private SinglePressButton right = new SinglePressButton();
    private SinglePressButton red = new SinglePressButton();
    private SinglePressButton blue = new SinglePressButton();
    private Field.AllianceColor alliance = Field.AllianceColor.BLUE;
    private DISTANCE distance = DISTANCE.SHORT;
    private DELAY delay = DELAY.NONE;

    @Override
    public void init() {

        // Placate drivers; sometimes VuforiaFTC is slow to init
        telemetry.addData(">", "Initializing...");
        telemetry.update();

        // Drive motors
        tank = new WheelMotorConfigs().init(hardwareMap, telemetry);
        tank.stop();

        // Lift
        lift = new MotorConfigs().init(hardwareMap, telemetry, "LIFT");
        lift.stop();

        // Claws
        clawTop = new ServoConfigs().init(hardwareMap, telemetry, "CLAW-TOP");
        clawTop.min();
        clawBottom = new ServoConfigs().init(hardwareMap, telemetry, "CLAW-BOTTOM");
        clawBottom.min();

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
        up.update(gamepad1.dpad_up);
        down.update(gamepad1.dpad_down);
        left.update(gamepad1.dpad_left);
        right.update(gamepad1.dpad_right);
        red.update(gamepad1.b);
        blue.update(gamepad1.x);

        // Adjust delay
        if (up.active()) {
            delay = delay.next();
        } else if (down.active()) {
            delay = delay.prev();
        }

        // Adjust distance
        if (right.active()) {
            distance = distance.next();
        } else if (left.active()) {
            distance = distance.prev();
        }

        // Adjust alliance color
        if (red.active()) {
            alliance = Field.AllianceColor.RED;
        } else if (blue.active()) {
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
        // Handle DriveTo driving
        if (drive != null) {
            // DriveTo
            drive.drive();

            // Return to teleop when complete
            if (drive.isDone()) {
                drive = null;
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
        if (drive != null || timer > time) {
            return;
        }

        // Main state machine
        switch (state) {
            case INIT:
                lift.setPower(LIFT_SPEED_UP);
                clawTop.max();
                clawBottom.max();
                timer = time + LIFT_DELAY;
                state = state.next();
                break;
            case READY:
                lift.stop();
                state = state.next();
                break;
            case DELAY:
                timer = time + delay.seconds();
                state = state.next();
                break;
            case DRIVE_FORWARD:
                drive = driveForward(this, tank, distance.millimeters());
                state = state.next();
                break;
            case RELEASE:
                clawTop.min();
                clawBottom.min();
                state = state.next();
                break;
            case RELEASE_DELAY:
                timer = time + 1;
                state = state.next();
                break;
            case DRIVE_BACKWARD:
                timer = time + .15;
                tank.setSpeed(-.5);
                state = state.next();
                break;
            case DRIVE_STOP: // Only necessary until driveBackward is used
                tank.stop();
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
        switch ((SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
                tank.stop();
                break;
        }
    }

    @Override
    public void driveToRun(DriveToParams param) {
        switch ((SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
                tank.setSpeed(SPEED_FORWARD_SLOW);
                break;
        }
    }

    @Override
    public double driveToSensor(DriveToParams param) {
        double value = 0;
        switch ((SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
                value = tank.getEncoder();
                break;
        }
        return value;
    }

    // Define the order of auto routine components
    enum AUTO_STATE implements OrderedEnum {
        INIT,
        READY,
        DELAY,
        DRIVE_FORWARD,
        RELEASE,
        RELEASE_DELAY,
        DRIVE_BACKWARD,
        DRIVE_STOP,
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