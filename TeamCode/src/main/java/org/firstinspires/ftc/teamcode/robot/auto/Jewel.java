package org.firstinspires.ftc.teamcode.robot.auto;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.teamcode.buttons.PAD_BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.field.Field;
import org.firstinspires.ftc.teamcode.field.VuforiaConfigs;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.robot.common.Common;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;
import org.firstinspires.ftc.teamcode.utils.Round;
import org.firstinspires.ftc.teamcode.vuforia.VuforiaFTC;

@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Jewel + Block", group = "Auto")
public class Jewel extends OpMode {

    // Auto constants
    private static final String TARGET = VuforiaConfigs.TargetNames[0];
    private static final int START_ANGLE = -4;
    private static final int START_DISTANCE = 1120;

    // Devices and subsystems
    private Robot robot = null;
    private Common common = null;
    private VuforiaFTC vuforia = null;

    // Runtime state
    private AutoDriver driver = new AutoDriver();
    private AUTO_STATE state = AUTO_STATE.LIFT_INIT;
    private boolean liftReady = false;
    private boolean targetReady = false;
    private boolean gameReady = false;
    private RelicRecoveryVuMark column = RelicRecoveryVuMark.UNKNOWN;

    // Init-time config
    private ButtonHandler buttons;
    private Field.AllianceColor alliance = Field.AllianceColor.RED;
    private DELAY delay = DELAY.NONE;
    private STONE stone = STONE.SAME_WALL;
    private MODE mode = MODE.JEWEL_BLOCK;

    @Override
    public void init() {
        telemetry.addData(">", "Init…");
        telemetry.update();

        // Init the common tasks elements
        robot = new Robot(hardwareMap, telemetry);
        common = robot.common;
        vuforia = robot.vuforia;

        // Init the camera system
        vuforia.start();

        // Register buttons
        buttons = new ButtonHandler(robot);
        buttons.register("MODE-UP", gamepad1, PAD_BUTTON.dpad_right);
        buttons.register("MODE-DOWN", gamepad1, PAD_BUTTON.dpad_left);
        buttons.register("DELAY-UP", gamepad1, PAD_BUTTON.dpad_up);
        buttons.register("DELAY-DOWN", gamepad1, PAD_BUTTON.dpad_down);
        buttons.register("STONE-UP", gamepad1, PAD_BUTTON.y);
        buttons.register("STONE-DOWN", gamepad1, PAD_BUTTON.a);
        buttons.register("ALLIANCE-RED", gamepad1, PAD_BUTTON.b);
        buttons.register("ALLIANCE-BLUE", gamepad1, PAD_BUTTON.x);
    }

    @Override
    public void init_loop() {

        // Zero the lift
        if (!liftReady) {
            // TODO: We need to zero the lift; for now just pretend
            liftReady = true;
        }

        // Process driver input
        buttons.update();
        mode = (MODE) updateEnum("MODE", mode);
        delay = (DELAY) updateEnum("DELAY", delay);
        stone = (STONE) updateEnum("STONE", stone);
        if (buttons.get("ALLIANCE-RED")) {
            alliance = Field.AllianceColor.RED;
        } else if (buttons.get("ALLIANCE-BLUE")) {
            alliance = Field.AllianceColor.BLUE;
        }

        // Update Vuforia tracking, when available
        if (vuforia.isRunning()) {
            vuforia.track();
            column = RelicRecoveryVuMark.from(vuforia.getTrackable(TARGET));
        }
        targetReady = (vuforia.isRunning() && !vuforia.isStale() && vuforia.getVisible(TARGET));

        // Driver setup
        telemetry.addData("Mode", mode);
        telemetry.addData("Alliance", alliance);
        telemetry.addData("Starting Stone", stone);
        telemetry.addData("Delay", delay);

        // Driver feedback
        telemetry.addData("\t\t\t", "");
        telemetry.addData("Start ∠",
                targetReady ? (vuforia.getTargetAngle(TARGET) - START_ANGLE) + "°" : "<Not Visible>");
        telemetry.addData("Start Distance",
                targetReady ? (vuforia.getX() - START_DISTANCE) + "mm" : "<Not Visible>");
        telemetry.addData("Column", column);
        telemetry.addData("Gyro", robot.gyro.isReady() ? "Ready" : "Calibrating…");
        telemetry.addData("Lift", liftReady ? "Ready" : "Zeroing");

        // Overall ready status
        gameReady = (robot.gyro.isReady() && targetReady && liftReady);
        telemetry.addData("\t\t\t", "");
        telemetry.addData(">", gameReady ? "Ready for game start" : "NOT READY");
        telemetry.update();
    }

    @Override
    public void start() {
        telemetry.clearAll();

        // Log if we didn't exit init as expected
        if (!gameReady) {
            telemetry.log().add("Started before ready");
        }

        // Disable the lift if it isn't ready
        if (!liftReady) {
            robot.lift.setEnabled(false);
            telemetry.log().add("Running without lift");
        }

        // Set the gyro offset, if available
        if (targetReady) {
            robot.gyro.setOffset(vuforia.getTargetAngle(TARGET));
            column = RelicRecoveryVuMark.from(robot.vuforia.getTrackable(TARGET));
        } else {
            telemetry.log().add("Running without target alignment");
        }

        // Steady…
        state = AUTO_STATE.values()[0];
    }

    @Override
    public void loop() {

        // Handle AutoDriver driving
        driver = common.drive.loop(driver);

        // Debug feedback
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
                state = state.next();
                break;
            case PARSE_JEWEL:
                driver = delegateDriver(common.jewel.parse(driver));
                break;
            case LIFT_INIT:
                driver = delegateDriver(common.lift.autoStart(driver));
                break;
            case HIT_JEWEL:
                driver = delegateDriver(common.jewel.hit(driver, alliance));
                break;
            case DELAY:
                if (mode == MODE.JEWEL_ONLY) {
                    state = AUTO_STATE.DONE;
                    break;
                }
                driver.interval = delay.seconds();
                state = state.next();
                break;
            case DRIVE_DOWN:
                driver.drive = common.drive.distance(275);
                state = state.next();
                break;
            case GYRO_WAIT:
                if (robot.gyro.isReady()) {
                    state = state.next();
                } else {
                    telemetry.log().add("Waiting for gyro…");
                }
                break;
            case PIVOT_ZERO:
                driver.drive = common.drive.heading(0);
                state = state.next();
                break;
            case DRIVE_FORWARD:
                driver.drive = common.drive.distance(525);
                state = state.next();
                break;
            case TURN_ACROSS:
                // 25° past 90/270
                driver.drive = (alliance == Field.AllianceColor.RED ?
                        common.drive.heading(115) :
                        common.drive.heading(245));
                state = state.next();
                break;
            case DRIVE_ACROSS:
                // TODO: Drive more/less based on target column
                driver.drive = common.drive.distance(1085);
                state = state.next();
                break;
            case PIVOT_TO_FACE:
                // TODO: Pivot more/less based on target column
                driver.drive = common.drive.heading(180);
                state = state.next();
                break;
            case DRIVE_TO_BOX:
                driver.drive = common.drive.distance(575);
                state = state.next();
                break;
            case EJECT:
                driver = delegateDriver(common.lift.eject(driver));
                break;
            case DONE:
                driver.done = true;
                break;
        }
    }

    // Define the order of auto routine components
    enum AUTO_STATE implements OrderedEnum {
        INIT,               // Initiate stuff
        PARSE_JEWEL,        // Parse which jewel is on which side
        LIFT_INIT,          // Initiate lift & grab block
        HIT_JEWEL,          // Turn to hit the jewel
        DELAY,              // Optionally wait for our alliance partner
        // End here if we are in JEWEL_ONLY mode
        DRIVE_DOWN,         // Get our front wheels off the stone
        GYRO_WAIT,          // Wait for the gyro
        // Hold indefinitely if the gyro isn't available
        PIVOT_ZERO,         // Pivot to a heading of 0
        DRIVE_FORWARD,      // Drive forward from the starting stone
        TURN_ACROSS,        // Pivot to drive across the field
        DRIVE_ACROSS,       // Drive to the spot between the starting stones
        PIVOT_TO_FACE,      // Pivot to face the rack
        DRIVE_TO_BOX,       // Drive up to the rack (light contact)
        EJECT,              // Release the block(s)
        DONE;               // Finish

        public Jewel.AUTO_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public Jewel.AUTO_STATE next() {
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

        public float seconds() {
            return milliseconds / 1000.0f;
        }

        public Jewel.DELAY prev() {
            return OrderedEnumHelper.prev(this);
        }

        public Jewel.DELAY next() {
            return OrderedEnumHelper.next(this);
        }
    }

    // Balance stone positions
    enum STONE implements OrderedEnum {
        SAME_WALL,
        CORNER_WALL;

        public Jewel.STONE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public Jewel.STONE next() {
            return OrderedEnumHelper.next(this);
        }
    }

    // Balance stone positions
    enum MODE implements OrderedEnum {
        JEWEL_BLOCK,
        JEWEL_ONLY;

        public Jewel.MODE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public Jewel.MODE next() {
            return OrderedEnumHelper.next(this);
        }
    }

    // Utility function to delegate our AutoDriver to an external provider
    // Driver is proxied back up to caller, state is advanced when delegate sets ::done
    private AutoDriver delegateDriver(AutoDriver autoDriver) {
        if (autoDriver.isDone()) {
            autoDriver.done = false;
            state = state.next();
        }
        return autoDriver;
    }

    // Process up/down buttons pairs for ordered enums
    private OrderedEnum updateEnum(String name, OrderedEnum e) {
        OrderedEnum retval = e;
        if (buttons.get(name + "-UP")) {
            retval = e.next();
        } else if (buttons.get(name + "-DOWN")) {
            retval = e.prev();
        }
        return retval;
    }
}


