package org.firstinspires.ftc.teamcode.robot.auto;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.buttons.PAD_BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.field.Field;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.robot.common.Common;
import org.firstinspires.ftc.teamcode.robot.common.Lift;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;
import org.firstinspires.ftc.teamcode.utils.Round;
import org.firstinspires.ftc.teamcode.vuforia.ImageFTC;

//@Disabled
@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Jewel + Block", group = "Auto")
public class JewelForward extends OpMode {

    // Auto constants
    private static final int RELEASE_REVERSE_MM = 125;
    private static final float RELEASE_DELAY = 0.5f;
    private static final int DRIVE_TO_BOX_MM = 900; // Not tested

    // Devices and subsystems
    private Robot robot = null;
    private Common common = null;

    // Runtime state
    private AutoDriver driver = new AutoDriver();
    private JewelForward.AUTO_STATE state = JewelForward.AUTO_STATE.LIFT_INIT;
    private boolean liftReady = false;

    // Init-time config
    private ButtonHandler buttons;
    private Field.AllianceColor alliance = Field.AllianceColor.BLUE;
    private JewelForward.DELAY delay = JewelForward.DELAY.NONE;

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
        buttons = new ButtonHandler(robot);
        buttons.register("DELAY-UP", gamepad1, PAD_BUTTON.dpad_up);
        buttons.register("DELAY-DOWN", gamepad1, PAD_BUTTON.dpad_down);
        buttons.register("ALLIANCE-RED", gamepad1, PAD_BUTTON.b);
        buttons.register("ALLIANCE-BLUE", gamepad1, PAD_BUTTON.x);
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

        // Adjust alliance color
        if (buttons.get("ALLIANCE-RED")) {
            alliance = Field.AllianceColor.RED;
        } else if (buttons.get("ALLIANCE-BLUE")) {
            alliance = Field.AllianceColor.BLUE;
        }

        // Driver feedback
        telemetry.addData("Alliance", alliance);
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
        state = JewelForward.AUTO_STATE.values()[0];
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
            case PIVOT_BACK:
                driver.drive = common.drive.heading(reverseOnAlliance(0));
                state = state.next();
                break;
            case PIVOT_BACK2:
                driver.drive = common.drive.heading(reverseOnAlliance(alliance == Field.AllianceColor.BLUE ? 3 : 7));
                state = state.next();
                break;
            case DRIVE_FORWARD:
                driver.drive = common.drive.distance(1000);
                state = state.next();
                break;
            case PIVOT135:
                driver.drive = (alliance == Field.AllianceColor.RED ?
                        common.drive.heading(reverseOnAlliance(115)) :
                        common.drive.heading(reverseOnAlliance(-55))); // Turn left and back up vs right and distance
                state = state.next();
                break;
            case DRIVE_DIAGONAL:
                driver.drive = (alliance == Field.AllianceColor.RED ?
                        common.drive.distance(1750) :
                        common.drive.distance(-1050));
                state = state.next();
                break;
            case PIVOT_TO_FACE:
                driver.drive = (alliance == Field.AllianceColor.RED ?
                        common.drive.heading(reverseOnAlliance(173)) :
                        common.drive.heading(reverseOnAlliance(-173)));
                state = state.next();
                break;
            case LOWER_LIFT:
                robot.lift.setPower(Lift.LIFT_SPEED_DOWN);
                driver.interval = Lift.LIFT_DELAY;
                state = state.next();
                break;
            case LOWER_LIFT_STOP:
                robot.lift.stop();
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
            case RELEASE_TURN:
                driver.drive = common.drive.degrees(reverseOnAlliance(5));
                state = state.next();
                break;
            case DONE:
                // Exit the opmode
                driver.done = true;
                break;
        }
    }

    // Probably should not be here
    private int reverseOnAlliance(int turnDegrees) {
        return (alliance == Field.AllianceColor.RED ? 1 : -1) * turnDegrees;
    }

    // Utility function to delegate our AutoDriver to an external provider
    // Driver is proxied back up to caller, state is advanced when delegate sets ::done
    private AutoDriver delegateDriver(AutoDriver autoDriver, JewelForward.AUTO_STATE next) {
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
        PIVOT_BACK,         // Pivot back to a heading of 0
        PIVOT_BACK2,        // Pivot back to the future
        DRIVE_FORWARD,      // Drive distance to appropriate point
        PIVOT135,           // Pivot to align with the desired rack
        DRIVE_DIAGONAL,     // drive to the spot between the balancing plates
        PIVOT_TO_FACE,      // Pivot to face the rack\
        LOWER_LIFT,         // Lower the lift so that we don't drop the block on the bottom claw
        LOWER_LIFT_STOP,    // Stop lowering the lift
        DRIVE_TO_BOX,       // Drive up to the rack
        RELEASE,            // Release the block
        RELEASE_TURN,       // Turn, so we push the block into a specific column if we hit an edge
        RELEASE_REVERSE,    // Reverse away from the block
        DONE;               // Finish

        public JewelForward.AUTO_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public JewelForward.AUTO_STATE next() {
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

        public float seconds() {
            return milliseconds / 1000.0f;
        }

        public JewelForward.DELAY prev() {
            return OrderedEnumHelper.prev(this);
        }

        public JewelForward.DELAY next() {
            return OrderedEnumHelper.next(this);
        }
    }
}


