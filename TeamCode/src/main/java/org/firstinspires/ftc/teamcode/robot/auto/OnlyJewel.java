package org.firstinspires.ftc.teamcode.robot.auto;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.buttons.BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.field.Field;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.robot.common.Common;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;
import org.firstinspires.ftc.teamcode.utils.Round;

@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Jewel Only", group = "Auto")
public class OnlyJewel extends OpMode {

    // Auto constants
    private static final double CLAW_DELAY = 0.5d;
    private static final double ARM_DELAY = 0.5d;
    private static final int JEWEL_PIVOT_DEGREES = 10;

    // Devices and subsystems
    private Robot robot = null;
    private Common common = null;

    // Runtime state
    private AutoDriver driver = new AutoDriver();
    private OnlyJewel.AUTO_STATE state = OnlyJewel.AUTO_STATE.LIFT_INIT;
    private boolean liftReady = false;

    // Init-time config
    private final ButtonHandler buttons = new ButtonHandler();
    private Field.AllianceColor alliance = Field.AllianceColor.BLUE;
    private WALL distance = WALL.ALLIANCE;
    private OnlyJewel.DELAY delay = OnlyJewel.DELAY.NONE;

    @Override
    public void init() {
        telemetry.addData(">", "Init…");
        telemetry.update();

        // Init the common tasks elements
        robot = new Robot(hardwareMap, telemetry);
        common = new Common(robot);

        // Init the camera system
        robot.vuforia.start();

        // Register buttons
        buttons.register("DELAY-UP", gamepad1, BUTTON.dpad_up);
        buttons.register("DELAY-DOWN", gamepad1, BUTTON.dpad_down);
        buttons.register("WALL-UP", gamepad1, BUTTON.dpad_right);
        buttons.register("WALL-DOWN", gamepad1, BUTTON.dpad_left);
        buttons.register("ALLIANCE-RED", gamepad1, BUTTON.b);
        buttons.register("ALLIANCE-BLUE", gamepad1, BUTTON.x);
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
        if (buttons.get("WALL-UP")) {
            distance = distance.next();
        } else if (buttons.get("WALL-DOWN")) {
            distance = distance.prev();
        }

        // Adjust alliance color
        if (buttons.get("ALLIANCE-RED")) {
            alliance = Field.AllianceColor.RED;
        } else if (buttons.get("ALLIANCE-BLUE")) {
            alliance = Field.AllianceColor.BLUE;
        }

        // Driver feedback
        telemetry.addData("Alliance", alliance);
        telemetry.addData("Distance", distance);
        telemetry.addData("Delay", delay);
        telemetry.addData("Gyro", robot.gyro.isReady() ? "Ready" : "Calibrating…");
        telemetry.addData("Lift", liftReady ? "Ready" : "Zeroing");
        if (robot.gyro.isReady() && liftReady) {
            telemetry.addData(">", "Ready for game start");
        }
        telemetry.update();
    }

    @Override
    public void start() {
        telemetry.clearAll();

        // Disable the lift if it isn't ready
        robot.lift.setEnabled(liftReady);

        // Steady...
        state = OnlyJewel.AUTO_STATE.values()[0];
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
        telemetry.addData("Running", driver.isRunning(time));
        telemetry.addData("Pivot CCW", common.jewel.getImage() != null ? common.jewel.pivotCCW(alliance) : "<No Image>");
        telemetry.addData("Gyro", Round.truncate(robot.gyro.getHeading()));
        telemetry.addData("Encoder", robot.wheels.getEncoder());
        telemetry.update();

        /*
         * Cut the loop short when we are AutoDriver'ing
         * This keeps us out of the state machine until the preceding command is complete
         */
        if (driver.isRunning(time)) {
            return;
        }

        // Main state machine, see enum for description of each state
        switch (state) {
            case INIT:
                driver.done = false;
                // Don't start driving until the gyro is ready
                // TODO: Do something different if the gyro never becomes available
                if (robot.gyro.isReady()) {
                    state = state.next();
                }
                break;
            case PARSE_JEWEL:
                driver = delegateDriver(common.jewel.parse(), state.next());
                break;
            case LIFT_INIT:
                driver = delegateDriver(common.lift.autoStart(), state.next());
                break;
            case HIT_JEWEL:
                driver = delegateDriver(common.jewel.hit(alliance), state.next());
                break;
            case DELAY:
                driver.interval = delay.seconds();
                state = state.next();
                break;
            case DONE:
                driver.done = true;
                break;
        }
    }

    // Utility function to delegate our AutoDriver to an external provider
    // Driver is proxied back up to caller, state is advanced when delegate sets ::done
    private AutoDriver delegateDriver(AutoDriver autoDriver, OnlyJewel.AUTO_STATE next) {
        if (autoDriver.isDone()) {
            autoDriver.done = false;
            state = next;
        }
        return autoDriver;
    }

    // Define the order of auto routine components
    enum AUTO_STATE implements OrderedEnum {
        INIT,               // Initiate stuff
        PARSE_JEWEL,        // Parse which jewel is on which side
        LIFT_INIT,          // Initiate lift & grab block
        HIT_JEWEL,          // Turn to hit the jewel
        DELAY,              // Optionally wait for our alliance partner
        DONE;               // Finish

        public OnlyJewel.AUTO_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public OnlyJewel.AUTO_STATE next() {
            return OrderedEnumHelper.next(this);
        }
    }

    // Configurable straight-line distance
    enum WALL implements OrderedEnum {
        // TODO: update so that this is accurate
        ALLIANCE(965),
        SHARED(1016);

        private final int millimeters;

        WALL(int millimeters) {
            this.millimeters = millimeters;
        }

        public int millimeters() {
            return millimeters;
        }

        public WALL prev() {
            return OrderedEnumHelper.prev(this);
        }

        public WALL next() {
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

        public OnlyJewel.DELAY prev() {
            return OrderedEnumHelper.prev(this);
        }

        public OnlyJewel.DELAY next() {
            return OrderedEnumHelper.next(this);
        }
    }
}

