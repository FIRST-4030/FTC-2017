package org.firstinspires.ftc.teamcode.robot.auto;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.field.Field;
import org.firstinspires.ftc.teamcode.robot.Robot;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.buttons.BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.field.Field;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;

@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Jewel Pivot", group = "Auto")
public class JewelPivot extends OpMode {

    // Auto constants
    private static final int RELEASE_REVERSE_MM = 250;
    private static final double RELEASE_DELAY = 0.5d;

    // Devices and subsystems
    private Robot robot = null;
    private CommonTasks common = null;

    // Runtime state
    private AutoDriver driver = new AutoDriver();
    private org.firstinspires.ftc.teamcode.robot.auto.JewelPivot.AUTO_STATE state = org.firstinspires.ftc.teamcode.robot.auto.JewelPivot.AUTO_STATE.LIFT_INIT;
    private boolean liftReady = false;

    // Init-time config
    private final ButtonHandler buttons = new ButtonHandler();
    private Field.AllianceColor alliance = Field.AllianceColor.BLUE;
    private org.firstinspires.ftc.teamcode.robot.auto.JewelPivot.DISTANCE distance = DISTANCE.SHORT;
    private org.firstinspires.ftc.teamcode.robot.auto.JewelPivot.DELAY delay = org.firstinspires.ftc.teamcode.robot.auto.JewelPivot.DELAY.NONE;

    @Override
    public void init() {

        // Placate drivers
        telemetry.addData(">", "Initializing...");
        telemetry.update();

        // Init the common tasks elements
        robot = new Robot(hardwareMap, telemetry);
        common = new CommonTasks(robot);

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

        robot.vuforia.start();
        robot.vuforia.enableCapture(true);
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
        robot.lift.setEnabled(liftReady);

        // Steady...
        state = JewelPivot.AUTO_STATE.values()[0];

        //capture an image and store it for use in jewel parsing later.

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
            case PARSE_JEWEL:
                //start vuforia and enable image capture
                
                state = state.next();
                break;
            case DELAY:
                driver.interval = delay.seconds();
                state = state.next();
                break;
            /*case HIT_JEWEL:
                state = state.next();
                break;*/
            /*case PIVOT_BACK:
                state = state.next();
                break;*/
            case DRIVE_FORWARD:
                driver.drive = common.driveForward(distance.millimeters());
                state = state.next();
                break;
            /*case PIVOT_TO_FACE:
                state = state.next();
                break;*/
            /*case DRIVE_TO_BOX:
                state = state.next();
                break;*/
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
    private AutoDriver delegateDriver(AutoDriver autoDriver, JewelPivot.AUTO_STATE next) {
        if (autoDriver.isDone()) {
            autoDriver.done = false;
            state = next;
        }
        return autoDriver;
    }

    // Define the order of auto routine components
    enum AUTO_STATE implements OrderedEnum {
        INIT,               //initiate stuff
        LIFT_INIT,          //intiate lift
        PARSE_JEWEL,        //parse which jewel is on which side
        DELAY,              //delay? we likely won't need this in the final version but just in case...
        HIT_JEWEL,          //pivot to hit the jewel
        PIVOT_BACK,         //pivot back to correct position? might not need this step
        DRIVE_FORWARD,      //drive forward to approprate point
        PIVOT_TO_FACE,      //pivot to align with the desired box
        DRIVE_TO_BOX,       //drive up to the box
        RELEASE,            //release the block
        RELEASE_REVERSE,    //reverse away from the blok
        DONE;               //finish

        public JewelPivot.AUTO_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public JewelPivot.AUTO_STATE next() {
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

        public JewelPivot.DISTANCE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public JewelPivot.DISTANCE next() {
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

