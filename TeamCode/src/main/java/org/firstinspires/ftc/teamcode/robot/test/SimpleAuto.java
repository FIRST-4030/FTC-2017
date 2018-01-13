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
import org.firstinspires.ftc.teamcode.wheels.MOTOR_SIDE;

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

        // Spinners
        buttons.spinners.add("DRIVE_INC",
                gamepad2, PAD_BUTTON.right_bumper, PAD_BUTTON.left_bumper,
                Round.magnitudeValue(Drive.DRIVE_PARAMS.P / 100.0d),
                Round.magnitudeValue(Drive.DRIVE_PARAMS.P / 10.0d));
        buttons.spinners.add("TURN_P",
                gamepad2, PAD_BUTTON.dpad_up, PAD_BUTTON.dpad_down,
                "DRIVE_INC", Drive.TURN_PARAMS.P);
        buttons.spinners.add("TURN_I",
                gamepad2, PAD_BUTTON.dpad_right, PAD_BUTTON.dpad_left,
                "DRIVE_INC", Drive.TURN_PARAMS.I);
        // Disable all spinners
        //buttons.spinners.setEnable(false);
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
        Drive.TURN_PARAMS.P = buttons.spinners.getFloat("TURN_P");
        Drive.TURN_PARAMS.I = buttons.spinners.getFloat("TURN_I");
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
        telemetry.addData("Wheels L/R", robot.wheels.getEncoder(MOTOR_SIDE.LEFT) +
                "/" + robot.wheels.getEncoder(MOTOR_SIDE.RIGHT));
        telemetry.addData("Wheels Rate L/R", Round.truncate(robot.wheels.getRate(MOTOR_SIDE.LEFT)) +
                "/" + Round.truncate(robot.wheels.getRate(MOTOR_SIDE.RIGHT)));
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

        // Wheel PID testing @ gamepad2
        float speed = 0.0f;
        if (gamepad2.a) {
            speed = 0.25f;
        } else if (gamepad2.b) {
            speed = 0.50f;
        } else if (gamepad2.x) {
            speed = 0.75f;
        } else if (gamepad2.y) {
            speed = -gamepad2.left_stick_y * 0.10f;
        }
        if (speed > 0.0d) {
            if (Math.abs(gamepad2.left_trigger) > 0.5) {
                robot.wheels.setSpeed(-speed, MOTOR_SIDE.LEFT);
                robot.wheels.setSpeed(speed, MOTOR_SIDE.RIGHT);
            } else if (Math.abs(gamepad2.right_trigger) > 0.5) {
                robot.wheels.setSpeed(speed, MOTOR_SIDE.LEFT);
                robot.wheels.setSpeed(-speed, MOTOR_SIDE.RIGHT);
            } else {
                robot.wheels.setSpeed(speed);
            }
        } else {
            robot.wheels.stop();
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
            speed = Drive.SPEED_FORWARD;
            if (buttons.get("REVERSE")) {
                speed *= -1;
            }
            driver.drive = common.drive.timeTurn(1000, speed);
        } else if (gamepad1.x) {
            speed = Drive.SPEED_FORWARD;
            if (buttons.get("REVERSE")) {
                speed *= -1;
            }
            driver.drive = common.drive.time(1000, speed);
        } else if (gamepad1.dpad_left) {
            driver.drive = common.drive.heading(270);
        } else if (gamepad1.dpad_right) {
            driver.drive = common.drive.heading(90);
        } else if (gamepad1.dpad_up) {
            driver.drive = common.drive.heading(0);
        } else if (gamepad1.dpad_down) {
            driver.drive = common.drive.heading(180);
        } else if (gamepad1.left_bumper) {
            driver.drive = common.drive.heading(315);
        } else if (gamepad1.right_bumper) {
            driver.drive = common.drive.heading(45);
        }
    }
}