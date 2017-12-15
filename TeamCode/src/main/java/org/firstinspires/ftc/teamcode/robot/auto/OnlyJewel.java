package org.firstinspires.ftc.teamcode.robot.auto;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.buttons.BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.field.Field;
import org.firstinspires.ftc.teamcode.robot.CLAWS;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;
import org.firstinspires.ftc.teamcode.vuforia.ImageFTC;

@Disabled
@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Jewel Only", group = "Auto")
public class OnlyJewel extends OpMode {

    // Auto constants
    private static final double CLAW_DELAY = 0.5d;
    private static final double ARM_DELAY = 0.5d;
    private static final int JEWEL_PIVOT_DEGREES = 10;

    // Devices and subsystems
    private Robot robot = null;
    private CommonTasks common = null;

    // Runtime state
    private AutoDriver driver = new AutoDriver();
    private OnlyJewel.AUTO_STATE state = OnlyJewel.AUTO_STATE.LIFT_INIT;
    private boolean liftReady = false;
    private boolean pivotLeft = false;
    private ImageFTC image = null;

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
        common = new CommonTasks(robot);

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
        telemetry.update();
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
        telemetry.addData("PivotLeft", pivotLeft); // will be false for the first bit
        telemetry.addData("Gyro", robot.gyro.getHeading());
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
            case ENABLE_CAPTURE:
                robot.vuforia.enableCapture(true);
                state = state.next();
                break;
            case WAIT_FOR_IMAGE:
                if (image == null) {
                    robot.vuforia.capture();
                    image = robot.vuforia.getImage();
                } else {
                    state = state.next();
                }
                break;
            case DISABLE_CAPTURE:
                robot.vuforia.enableCapture(false);
                state = state.next();
                break;
            case PARSE_JEWEL:
                // Determine which way to pivot to remove the other alliance's jewel
                pivotLeft = (common.leftJewelRed(image)) == (alliance == Field.AllianceColor.BLUE);
                // Save the parsed image for future analysis
                common.drawJewelOutline(image);
                image.savePNG("auto-" + System.currentTimeMillis() + ".png");
                state = state.next();
                break;
            case GRAB_BLOCK:
                robot.claws[CLAWS.TOP.ordinal()].max();
                driver.interval = CLAW_DELAY;
                state = state.next();
                break;
            case LIFT_INIT:
                driver = delegateDriver(common.liftAutoStart(), state.next());
                break;
            case DEPLOY_ARM:
                robot.jewelArm.max();
                driver.interval = ARM_DELAY;
                state = state.next();
                break;
            case DELAY:
                driver.interval = delay.seconds();
                state = state.next();
                break;
            case HIT_JEWEL:
                //turns -90 if we're hitting the left jewel, 90 if we're hitting the right.
                driver.drive = common.turnDegrees((pivotLeft ? -1 : 1) * JEWEL_PIVOT_DEGREES);
                state = state.next();
                break;
            case RETRACT_ARM:
                robot.jewelArm.setPosition(CommonTasks.JEWEL_ARM_RETRACT);
                driver.interval = ARM_DELAY;
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
        ENABLE_CAPTURE,     // Enable vuforia image capture
        WAIT_FOR_IMAGE,     // Make sure we don't try to do anything before Vuforia returns an image to analyze.
        DISABLE_CAPTURE,    // Disable vuforia capture so we run faster (?)
        PARSE_JEWEL,        // Parse which jewel is on which side
        GRAB_BLOCK,         // Grab the block in front of us
        LIFT_INIT,          // Initiate lift
        DEPLOY_ARM,         // Move the arm down so we can hit the jewel
        DELAY,              // Delay? we likely won't need this in the final version but just in case..
        // We might need this in the final to let the arm come all the way down
        HIT_JEWEL,          // Pivot to hit the correct jewel
        RETRACT_ARM,        // Retract the arm so we don't accidentally hit the jewels again
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

        private int millimeters;

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

        public OnlyJewel.DELAY prev() {
            return OrderedEnumHelper.prev(this);
        }

        public OnlyJewel.DELAY next() {
            return OrderedEnumHelper.next(this);
        }
    }
}

