package org.firstinspires.ftc.teamcode.robot.auto;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.buttons.BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.field.Field;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;

@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Straight Line", group = "Auto")
public class StraightLine extends OpMode {

    // Auto constants
    private static final int RELEASE_REVERSE_MM = 250;
    private static final double RELEASE_DELAY = 0.5d;

    // Devices and subsystems
    private Robot robot = null;
    private CommonTasks common = null;

    // Runtime state
    private AutoDriver driver = new AutoDriver();
    private AUTO_STATE state = AUTO_STATE.LIFT_INIT;
    private boolean liftReady = false;

    // Init-time config
    private final ButtonHandler buttons = new ButtonHandler();
    private Field.AllianceColor alliance = Field.AllianceColor.BLUE;
    private DISTANCE distance = DISTANCE.SHORT;
    private DELAY delay = DELAY.NONE;

    @Override
    public void init() {

        // Init the robot and common tasks
        robot = new Robot(hardwareMap, telemetry);
        common = new CommonTasks(robot);

        // Register buttons
        buttons.register("DELAY-UP", gamepad1, BUTTON.dpad_up);
        buttons.register("DELAY-DOWN", gamepad1, BUTTON.dpad_down);
        buttons.register("DISTANCE-UP", gamepad1, BUTTON.dpad_right);
        buttons.register("DISTANCE-DOWN", gamepad1, BUTTON.dpad_left);
        buttons.register("ALLIANCE-" + Field.AllianceColor.RED, gamepad1, BUTTON.b);
        buttons.register("ALLIANCE-" + Field.AllianceColor.BLUE, gamepad1, BUTTON.x);

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

        // Update the buttons
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
        for (Field.AllianceColor color : Field.AllianceColor.values()) {
            if (buttons.get("ALLIANCE-" + color)) {
                alliance = color;
            }
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
        robot.lift.setEnabled(liftReady);

        // Bring the arm to the runtime retracted position
        robot.jewelArm.setPosition(CommonTasks.JEWEL_ARM_RETRACT);

        // Steady...
        state = AUTO_STATE.values()[0];
    }

    @Override
    public void loop() {
        // Handle AutoDriver driving
        if (driver.drive != null) {
            // DriveTo
            driver.drive.drive();

            // Return to teleop when complete
            if (driver.drive.isDone()) {
                driver.drive = null;
                robot.wheels.setTeleop(true);
            }
        }

        // Driver feedback
        telemetry.addData("State", state);
        telemetry.addData("Encoder", robot.wheels.getEncoder());
        telemetry.update();

        /*
         * Cut the loop short when we are AutoDriver'ing
         * This keeps us out of the state machine until the preceding command is complete
         */
        if (driver.isRunning(time)) {
            return;
        }

        // Main state machine
        switch (state) {
            case INIT:
                driver.done = false;
                state = state.next();
                break;
            case LIFT_INIT:
                driver = delegateDriver(common.liftAutoStart(), state.next());
                break;
            case DELAY:
                driver.interval = delay.seconds();
                state = state.next();
                break;
            case DRIVE_FORWARD:
                driver.drive = common.driveForward(distance.millimeters());
                state = state.next();
                break;
            case RELEASE:
                for (ServoFTC claw : robot.claws) {
                    claw.min();
                }
                driver.interval = RELEASE_DELAY;
                state = state.next();
                break;
            case RELEASE_REVERSE:
                driver.drive = common.driveBackward(RELEASE_REVERSE_MM);
                state = state.next();
                break;
            case DONE:
                // Exit the opmode
                driver.done = true;
                this.requestOpModeStop();
                break;
        }
    }

    // Utility function to delegate our AutoDriver to an external provider
    // Driver is proxied back up to caller, state is advanced when delegate sets ::done
    private AutoDriver delegateDriver(AutoDriver autoDriver, AUTO_STATE next) {
        if (autoDriver.isDone()) {
            autoDriver.done = false;
            state = next;
        }
        return autoDriver;
    }

    // Define the order of auto routine components
    enum AUTO_STATE implements OrderedEnum {
        INIT,
        LIFT_INIT,
        DELAY,
        DRIVE_FORWARD,
        RELEASE,
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

        private final int millimeters;

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

        private final int milliseconds;

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