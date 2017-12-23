package org.firstinspires.ftc.teamcode.robot.auto;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.buttons.BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.field.Field;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.robot.common.Common;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;
import org.firstinspires.ftc.teamcode.vuforia.ImageFTC;

@Disabled
@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Jewel Pivot", group = "Auto")
public class JewelPivot extends OpMode {

    // Auto constants
    private static final int RELEASE_REVERSE_MM = 250;
    private static final double RELEASE_DELAY = 0.5d;
    private static final int DRIVE_TO_BOX_MM = 330; // Not tested
    private static final double CLAW_DELAY = 0.5d;
    private static final double ARM_DELAY = 0.5d;
    private static final int JEWEL_PIVOT_DEGREES = 10;

    // Devices and subsystems
    private Robot robot = null;
    private Common common = null;

    // Runtime state
    private AutoDriver driver = new AutoDriver();
    private JewelPivot.AUTO_STATE state = JewelPivot.AUTO_STATE.LIFT_INIT;
    private boolean liftReady = false;
    private ImageFTC image = null;

    // Init-time config
    private final ButtonHandler buttons = new ButtonHandler();
    private Field.AllianceColor alliance = Field.AllianceColor.BLUE;
    private WALL distance = WALL.ALLIANCE;
    private JewelPivot.DELAY delay = JewelPivot.DELAY.NONE;

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
        state = JewelPivot.AUTO_STATE.values()[0];
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
            case PIVOT_BACK_TO_0:
                driver.drive = common.drive.degrees((common.jewel.pivotCCW(alliance) ? 1 : -1) * JEWEL_PIVOT_DEGREES);
                state = state.next();
                break;
            case DRIVE_OFF_PLATFORM:
                driver.drive = common.drive.distance(610);
                state = state.next();
                break;
            case PIVOT_TO_MIDDLE:
                driver.drive = common.drive.heading((alliance == Field.AllianceColor.BLUE ? -1 : 1) * 90);
                state = state.next();
                break;
            case DRIVE_FORWARD:
                driver.drive = common.drive.distance(distance.millimeters());
                state = state.next();
                break;
            case PIVOT_TO_FACE:
                driver.drive = common.drive.heading(180);
                state = state.next();
                break;
            case DRIVE_TO_BOX:
                driver.drive = common.drive.distance(DRIVE_TO_BOX_MM);
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
                driver.drive = common.drive.distance(-RELEASE_REVERSE_MM);
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
    private AutoDriver delegateDriver(AutoDriver autoDriver, JewelPivot.AUTO_STATE next) {
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
        PIVOT_BACK_TO_0,    //pivots back to face origonal direction
        DRIVE_OFF_PLATFORM, //drives off platform to make turning easier.
        //should we add a true "Pivot_Back" state which returns us to 0 heading before doing other stuff?
        //it may be easier to do given that turning on the balance board kinda sucks.
        PIVOT_TO_MIDDLE,    // Pivot so we face the middle
        DRIVE_FORWARD,      // Drive distance to appropriate point
        PIVOT_TO_FACE,      // Pivot to align with the desired rack
        DRIVE_TO_BOX,       // Drive up to the rack
        RELEASE,            // Release the block
        RELEASE_REVERSE,    // Reverse away from the block
        DONE;               // Finish

        public JewelPivot.AUTO_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public JewelPivot.AUTO_STATE next() {
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

        public JewelPivot.DELAY prev() {
            return OrderedEnumHelper.prev(this);
        }

        public JewelPivot.DELAY next() {
            return OrderedEnumHelper.next(this);
        }
    }
}

