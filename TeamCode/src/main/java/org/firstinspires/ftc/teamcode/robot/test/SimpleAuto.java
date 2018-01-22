package org.firstinspires.ftc.teamcode.robot.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.buttons.BUTTON_TYPE;
import org.firstinspires.ftc.teamcode.buttons.PAD_BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.driveto.PID;
import org.firstinspires.ftc.teamcode.field.Field;
import org.firstinspires.ftc.teamcode.field.VuforiaConfigs;
import org.firstinspires.ftc.teamcode.robot.common.Common;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.robot.common.Drive;
import org.firstinspires.ftc.teamcode.robot.common.Lift;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;
import org.firstinspires.ftc.teamcode.utils.Round;

@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Simple Auto", group = "Test")
public class SimpleAuto extends OpMode {

    // Devices and subsystems
    private Robot robot = null;
    private Common common = null;
    private ButtonHandler buttons = null;
    private AutoDriver driver = new AutoDriver();
    private final PID pid = new PID();

    // Lift zero testing
    enum LIFT_STATE implements OrderedEnum {
        TIMEOUT,
        INIT,
        RETRACT,
        READY,
        DONE;

        public LIFT_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public LIFT_STATE next() {
            return OrderedEnumHelper.next(this);
        }
    }

    private LIFT_STATE liftState = LIFT_STATE.INIT;
    private static final int LIFT_TIMEOUT = 1500;
    // In general you should init false, but for testing start with nothing
    private boolean liftReady = true;
    private float liftTimeout = 0.0f;

    @Override
    public void init() {
        telemetry.addData(">", "Init…");
        telemetry.update();

        // Common init
        robot = new Robot(hardwareMap, telemetry);
        common = robot.common;

        // Buttons
        buttons = new ButtonHandler(robot);
        buttons.register("REVERSE", gamepad1, PAD_BUTTON.left_trigger, BUTTON_TYPE.TOGGLE);
        buttons.register("VUFORIA", gamepad1, PAD_BUTTON.left_stick_button);
    }

    @Override
    public void init_loop() {
        telemetry.addData("Gyro", robot.gyro.isReady() ? "Ready" : "Calibrating…");
        if (robot.gyro.isReady()) {
            telemetry.addData(">", "Ready for game start");
        }
        telemetry.update();
    }

    @Override
    public void start() {
        telemetry.clearAll();
    }

    @Override
    public void loop() {

        // Input
        buttons.update();
        if (buttons.get("VUFORIA") && !robot.vuforia.isRunning()) {
            robot.vuforia.start();
        }

        // Vuforia tracking, when available
        String vuforiaAngle = "<Not Visible>";
        if (robot.vuforia.isRunning()) {
            robot.vuforia.track();
            if (!robot.vuforia.isStale() && robot.vuforia.getVisible(VuforiaConfigs.TargetNames[0])) {
                vuforiaAngle = robot.vuforia.getTargetAngle(VuforiaConfigs.TargetNames[0]) + "°";
            }
        }

        // Handle AutoDriver driving
        driver = common.drive.loop(driver);

        // PID rate tracking
        pid.input((int) pid.last + 1);
        telemetry.addData("PID", Round.truncate(pid.rate) + "\t\t" +
                Round.truncate(pid.last) + "\t\t" + Round.truncate(pid.last / time));

        // Driver feedback
        telemetry.addData("Vuforia Angle", vuforiaAngle);
        telemetry.addData("LiftZero", liftState);
        telemetry.addData("Lift", robot.lift.getEncoder() +
                "/" + (robot.liftSwitch.get() ? "Down" : "Up") +
                " (" + liftState + ")");
        telemetry.addData("Gyro", robot.gyro.isReady() ? Round.truncate(robot.gyro.getHeading()) : "<Calibrating>");
        telemetry.addData("Time/Drive", Round.truncate(time) + "/" + driver.drive);
        telemetry.update();

        /*
         * Cut the loop short when we are AutoDriver'ing
         * This keeps us out of the state machine until the preceding command is complete
         */
        if (driver.isRunning(time)) {
            return;
        }

        // Test lift zero, with persistent timeout
        if (!liftReady) {
            switch (liftState) {
                case INIT:
                    liftTimeout = (float) time + (LIFT_TIMEOUT / 1000);
                    liftState = liftState.next();
                    break;
                case RETRACT:
                    if (robot.liftSwitch.get()) {
                        liftState = liftState.next();
                    } else if (time > liftTimeout) {
                        robot.lift.stop();
                        liftState = LIFT_STATE.TIMEOUT;
                    } else {
                        robot.lift.setPower(Lift.LIFT_SPEED_DOWN);
                    }
                    break;
                case READY:
                    robot.lift.stop();
                    robot.lift.resetEncoder();
                    liftState = liftState.next();
                    break;
                case DONE:
                    liftReady = true;
                    break;
            }
        }

        if (gamepad1.a) {
            int distance = (int) (Field.MM_PER_INCH * 20);
            if (buttons.get("REVERSE")) {
                distance *= -1;
            }
            driver.drive = common.drive.distance(distance);
        } else if (gamepad1.y) {
            Field.AllianceColor color = Field.AllianceColor.RED;
            if (buttons.get("REVERSE")) {
                color = Field.AllianceColor.opposite(color);
            }
            driver = common.jewel.hit(driver, color);
            if (driver.isDone()) {
                common.jewel.reset();
            }
        } else if (gamepad1.b) {
            float speed = Drive.SPEED_FORWARD;
            if (buttons.get("REVERSE")) {
                speed *= -1;
            }
            driver.drive = common.drive.timeTurn(1000, speed);
        } else if (gamepad1.x) {
            float speed = Drive.SPEED_FORWARD;
            if (buttons.get("REVERSE")) {
                speed *= -1;
            }
            driver.drive = common.drive.time(1000, speed);
        }
    }
}