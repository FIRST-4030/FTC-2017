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
import org.firstinspires.ftc.teamcode.robot.common.Lights;
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
    private static final int COLUMN_ANGLE_OFFSET = 13; // unused
    private static final int COLUMN_DISTANCE_OFFSET = 205;
    private static final int COLUMN_DRIVETO_OFFSET = 150;
    private static final int EARLY_PILE_DISTANCE = 650;
    private static final int COLUMN_CORNER_OFFSET = 175;

    // Devices and subsystems
    private Robot robot = null;
    private Common common = null;
    private VuforiaFTC vuforia = null;
    private Lights lights = null;

    // Runtime state
    private AutoDriver driver = new AutoDriver();
    private AUTO_STATE state = AUTO_STATE.LIFT_INIT;
    private boolean liftReady = false;
    private boolean targetReady = false;
    private boolean gameReady = false;
    private RelicRecoveryVuMark column = RelicRecoveryVuMark.UNKNOWN;
    private Lights.MODE lightsMode = Lights.MODE.OFF;
    private double liftTimer = 0;

    // Init-time config
    private ButtonHandler buttons;
    private Field.AllianceColor alliance = Field.AllianceColor.RED;
    private STONE stone = STONE.SAME_WALL;
    private MODE mode = MODE.JEWEL_BLOCK;
    private EXTRA_BLOCK extraBlock = EXTRA_BLOCK.NONE;

    @Override
    public void init() {
        telemetry.addData(">", "Init…");
        telemetry.update();

        // Init the common tasks elements
        robot = new Robot(hardwareMap, telemetry);
        lights = new Lights(robot, lightsMode);
        common = robot.common;
        vuforia = robot.vuforia;

        // Init the camera system
        vuforia.start();
        vuforia.enableCapture(true);

        // Register buttons
        buttons = new ButtonHandler(robot);
        buttons.register("MODE-UP", gamepad1, PAD_BUTTON.dpad_right);
        buttons.register("MODE-DOWN", gamepad1, PAD_BUTTON.dpad_left);
        buttons.register("STONE-UP", gamepad1, PAD_BUTTON.y);
        buttons.register("STONE-DOWN", gamepad1, PAD_BUTTON.a);
        buttons.register("ALLIANCE-RED", gamepad1, PAD_BUTTON.b);
        buttons.register("ALLIANCE-BLUE", gamepad1, PAD_BUTTON.x);

        buttons.register("EXTRA_BLOCK-UP", gamepad1, PAD_BUTTON.dpad_up);
        buttons.register("EXTRA_BLOCK-DOWN", gamepad1, PAD_BUTTON.dpad_down);
    }

    @Override
    public void init_loop() {

        // Zero the lift
        if (!liftReady) {
            // TODO: We need to zero the lift; for now just pretend
            liftReady = true;
        }

        // Lights
        lights.loop(lightsMode);

        // Process driver input
        buttons.update();
        mode = (MODE) updateEnum("MODE", mode);
        stone = (STONE) updateEnum("STONE", stone);
        extraBlock = (EXTRA_BLOCK) updateEnum("EXTRA_BLOCK", extraBlock);
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
        telemetry.addData("Extra Block", extraBlock);

        // Positioning feedback
        telemetry.addData("\t\t\t", "");
        telemetry.addData("Start ∠",
                targetReady ? (vuforia.getTargetAngle(TARGET) - START_ANGLE) + "°" : "<Not Visible>");
        telemetry.addData("Start Distance",
                targetReady ? (vuforia.getX() - START_DISTANCE) + "mm" : "<Not Visible>");

        // Overall ready status
        gameReady = (robot.gyro.isReady() && targetReady && liftReady);
        telemetry.addData("\t\t\t", "");
        telemetry.addData(">", gameReady ? "Ready for game start" : "NOT READY");

        // Detailed feedback
        telemetry.addData("\t\t\t", "");
        telemetry.addData("Gyro", robot.gyro.isReady() ? "Ready" : "Calibrating…");
        telemetry.addData("Column", column);
        telemetry.addData("Lift", liftReady ? "Ready" : "Zeroing");

        // Update
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
            vuforia.track();
        } else {
            telemetry.log().add("Running without target alignment");
        }

        // Grab an image
        vuforia.capture();

        // Steady…
        state = AUTO_STATE.values()[0];
        lightsMode = Lights.MODE.AUTO_INIT;
    }

    @Override
    public void loop() {

        // Handle AutoDriver driving
        driver = common.drive.loop(driver);

        if(liftTimer > 0 && liftTimer < time){
            robot.lift.stop();
            liftTimer = 0;
        }


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
            case LIFT_INIT:
                lightsMode = Lights.MODE.OFF;
                driver = delegateDriver(common.lift.autoStart(driver));
                break;
            case JEWEL_SKIP:
                if(mode == MODE.BLOCK_ONLY){
                    vuforia.enableCapture(false);
                    state = AUTO_STATE.DRIVE_DOWN;
                } else {
                    state = state.next();
                }
                break;
            case PARSE_JEWEL:
                driver = delegateDriver(common.jewel.parse(driver));
                break;
//            case JEWEL_ADJUST:
//                int jewelOffset = vuforia.getX() - START_DISTANCE;
//                if(Math.abs(jewelOffset) > 10) driver.drive = common.drive.distance(jewelOffset);
//                state = state.next();
//                break;
            case HIT_JEWEL:
                driver = delegateDriver(common.jewel.hit(driver, alliance));
                break;
            case JEWEL_END:
                if (mode == MODE.JEWEL_ONLY) {
                    state = AUTO_STATE.DONE;
                } else {
                    state = state.next();
                }
                break;
            case DRIVE_DOWN:
                driver.drive = common.drive.distance(300);
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
                driver.drive = common.drive.distance(500);
                state = state.next();
                break;
            case TURN_ACROSS:
                if(stone == STONE.SAME_WALL) {
                    // 25° past 90/270
                    driver.drive = (alliance == Field.AllianceColor.RED ?
                            common.drive.heading(115) :
                            common.drive.heading(245));
                } else {
                    driver.drive = (alliance == Field.AllianceColor.RED ?
                            common.drive.heading(90) :
                            common.drive.heading(270));
                }
                state = state.next();
                break;
            case DRIVE_ACROSS:
                if(stone == STONE.SAME_WALL) {
                    int driveAcrossDistance = 1085;
                    if (column != RelicRecoveryVuMark.CENTER && column != RelicRecoveryVuMark.UNKNOWN) {
                        driveAcrossDistance += COLUMN_DISTANCE_OFFSET
                                * (alliance == Field.AllianceColor.BLUE ? -1 : 1)
                                * (column == RelicRecoveryVuMark.LEFT ? 1 : -0.975);
                    }
                    driver.drive = common.drive.distance(driveAcrossDistance);
                } else {
                    driver.drive = common.drive.distance(680);
                }
                state = state.next();
                break;
            case CORNER_WALL_SKIP:
                if (stone != STONE.CORNER_WALL) {
                    state = AUTO_STATE.EXTRA_BLOCK_PILE_SKIP;
                } else {
                    state = state.next();
                }
                break;
            case TURN_PERPENDICULAR:
                driver.drive = common.drive.heading(180);
                state = state.next();
                break;
            case DRIVE_ADJACENT:
                int cornerDescision = 455;
                if(column != RelicRecoveryVuMark.CENTER && column != RelicRecoveryVuMark.UNKNOWN){
                    cornerDescision += COLUMN_CORNER_OFFSET
                            * (column == RelicRecoveryVuMark.LEFT ? -1 : 1)
                            * (alliance == Field.AllianceColor.BLUE ? -1 : 1);
                }
                driver.drive = common.drive.distance(cornerDescision);
                state = AUTO_STATE.PIVOT_TO_FACE;
                break;
            case EXTRA_BLOCK_PILE_SKIP:
                if (extraBlock != EXTRA_BLOCK.PILE_FIRST || stone == STONE.CORNER_WALL) {
                    state = AUTO_STATE.PIVOT_TO_FACE;
                } else {
                    state = state.next();
                }
                break;
            case TURN_TO_PILE_EARLY:
                driver.drive = common.drive.heading(0);
                state = state.next();
                break;
            case DRIVE_TO_PILE_EARLY:
                driver.drive = common.drive.distance(EARLY_PILE_DISTANCE);
                state = state.next();
                break;
            case REVERSE_OUT_EARLY:
                driver.drive = common.drive.distance(-EARLY_PILE_DISTANCE);
                state = state.next();
                break;
            case PIVOT_TO_FACE:
                if(stone == STONE.SAME_WALL) {
                    driver.drive = common.drive.heading(180);
                } else {
                    driver.drive = common.drive.heading((alliance == Field.AllianceColor.BLUE ? 270 : 90));
                }
                state = state.next();
                break;
            case DRIVE_TO_BOX:
                if(stone == STONE.SAME_WALL) {
                    int toBoxDistance = 557;
                    if (column != RelicRecoveryVuMark.CENTER && column != RelicRecoveryVuMark.UNKNOWN) {
                        toBoxDistance += COLUMN_DRIVETO_OFFSET
                                * (column == RelicRecoveryVuMark.LEFT ? -1 : 1)
                                * (alliance == Field.AllianceColor.BLUE ? -1 : 1);
                    }
                    driver.drive = common.drive.distance(toBoxDistance);
                } else {
                    driver.drive = common.drive.distance(335); // guess
                }
                state = state.next();
                break;
            case EJECT:
                driver = delegateDriver(common.lift.eject(driver));
                break;
            case EXTRA_BLOCK_BOX_END:
                if (extraBlock != EXTRA_BLOCK.BOX_FIRST || stone == STONE.CORNER_WALL) {
                    state = AUTO_STATE.DONE;
                } else {
                    state = state.next();
                }
                break;
            case REVERSE_AWAY:
                driver.drive = common.drive.distance(-300);
                state = state.next();
                break;
            case TURN_TO_PILE:
                driver.drive = common.drive.heading(0);
                state = state.next();
                break;
            case DRIVE_TO_PILE:
                common.lift.intake(driver);
                driver.drive = common.drive.distance(900);
                state = state.next();
                break;
            case REVERSE_OUT:
                robot.lift.setPower(1);
                liftTimer = time + .5;
                driver.drive = common.drive.distance(-900);
                state = state.next();
                break;
            case PIVOT_TO_FACE_2:
                driver.drive = common.drive.heading(180);
                state = state.next();
                break;
            case DRIVE_TO_BOX_2:
                driver.drive = common.drive.distance(300);
                state = state.next();
                break;
            case EJECT_2:
                driver = delegateDriver(common.lift.eject(driver));
                break;
            case DONE:
                lightsMode = Lights.MODE.OFF;
                driver.done = true;
                break;
        }
    }

    // Define the order of auto routine components
    enum AUTO_STATE implements OrderedEnum {
        INIT,               // Initiate stuff
        LIFT_INIT,          // Initiate lift & grab block
        JEWEL_SKIP,         // skips the jewel code if in BLOCK_ONLY mode
        PARSE_JEWEL,        // Parse which jewel is on which side
//        JEWEL_ADJUST,       // Move slightly forward if we are too close to the jewel to hit it
        HIT_JEWEL,          // Turn to hit the jewel
        JEWEL_END,              // Optionally wait for our alliance partner
        // End here if we are in JEWEL_ONLY mode
        DRIVE_DOWN,         // Get our front wheels off the stone
        GYRO_WAIT,          // Wait for the gyro
        // Hold indefinitely if the gyro isn't available
        PIVOT_ZERO,         // Pivot to a heading of 0
        DRIVE_FORWARD,      // Drive forward from the starting stone
        TURN_ACROSS,        // Pivot to drive across the field
        DRIVE_ACROSS,       // Drive to the spot between the starting stones
        CORNER_WALL_SKIP,
        // Skips a few instructions if we are on the corner wall
        TURN_PERPENDICULAR, // Turn to heading 180
        DRIVE_ADJACENT,
        EXTRA_BLOCK_PILE_SKIP,
        // Skips a few instructions if we aren't in PILE_FIRST extra block
        TURN_TO_PILE_EARLY, // Turn to the pile so we an grab more blocks
        DRIVE_TO_PILE_EARLY,    // Drives into the pile, hopefully grabbing another block
        REVERSE_OUT_EARLY,  // Reverse out of the pile
        // This is where the auto skips to if we aren't in PILE_FIRST
        PIVOT_TO_FACE,      // Pivot to face the rack
        DRIVE_TO_BOX,       // Drive up to the rack (light contact)
        EJECT,              // Release the block(s)
        EXTRA_BLOCK_BOX_END,
        // End here if we aren't in BOX_FIRST extra block
        REVERSE_AWAY,       // Back away from the box so our bot doesn't descore the block by turning
        TURN_TO_PILE,       // Turn back to the pile
        DRIVE_TO_PILE,      // Dive into the pile, hoping to pick up blocks
        REVERSE_OUT,        // Drives backwards out of the pile. Also move the lift up for .5 secs
        PIVOT_TO_FACE_2,    // pivots to face the rack again
        DRIVE_TO_BOX_2,     // Drives to the box again
        EJECT_2,            // Ejects the blocks again
        DONE;               // Finish

        public Jewel.AUTO_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public Jewel.AUTO_STATE next() {
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
        JEWEL_ONLY,
        BLOCK_ONLY;

        public Jewel.MODE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public Jewel.MODE next() {
            return OrderedEnumHelper.next(this);
        }
    }


    // A temporary enum for how the robot chooses which column
    enum EXTRA_BLOCK implements OrderedEnum {
        NONE,
        PILE_FIRST,
        BOX_FIRST;

        public Jewel.EXTRA_BLOCK prev() {
            return OrderedEnumHelper.prev(this);
        }

        public Jewel.EXTRA_BLOCK next() {
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