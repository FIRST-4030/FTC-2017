package org.firstinspires.ftc.teamcode.robot.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.buttons.BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.buttons.SPINNER_TYPE;
import org.firstinspires.ftc.teamcode.buttons.SpinnerHandler;
import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.driveto.DriveToComp;
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
    private double liftTimeout = 0;

    @Override
    public void init() {
        telemetry.addData(">", "Init…");
        telemetry.update();

        // Common init
        robot = new Robot(hardwareMap, telemetry);
        common = new Common(robot);

        // Buttons
        buttons = new ButtonHandler(robot);
        buttons.spinners.add("TURN_INC", SPINNER_TYPE.DOUBLE,
                gamepad2, BUTTON.right_bumper, BUTTON.left_bumper,
                Drive.TURN_PARAMS.P / 100.0d, Drive.TURN_PARAMS.P / 10.0d);
        buttons.spinners.add("TURN_P", SPINNER_TYPE.DOUBLE,
                gamepad2, BUTTON.dpad_up, BUTTON.dpad_down,
                "TURN_INC", Drive.TURN_PARAMS.P);
        buttons.spinners.add("TURN_I", SPINNER_TYPE.DOUBLE,
                gamepad2, BUTTON.dpad_right, BUTTON.dpad_left,
                "TURN_INC", Drive.TURN_PARAMS.I);
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
        Drive.TURN_PARAMS.P = buttons.spinners.getDouble("TURN_P");
        Drive.TURN_PARAMS.I = buttons.spinners.getDouble("TURN_I");

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
        telemetry.addData("Wheels L/R", robot.wheels.getEncoder(MOTOR_SIDE.LEFT) +
                "/" + robot.wheels.getEncoder(MOTOR_SIDE.RIGHT));
        telemetry.addData("Wheels Rate L/R", Round.truncate(robot.wheels.getRate(MOTOR_SIDE.LEFT)) +
                "/" + Round.truncate(robot.wheels.getRate(MOTOR_SIDE.RIGHT)));
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
        double speed = 0.0d;
        if (gamepad2.a) {
            speed = 0.25d;
        } else if (gamepad2.b) {
            speed = 0.50d;
        } else if (gamepad2.x) {
            speed = 0.75d;
        } else if (gamepad2.y) {
            speed = -gamepad2.left_stick_y * 0.10d;
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
                    liftTimeout = time + (LIFT_TIMEOUT / 1000);
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
            driver.drive = common.drive.distance((int) (25.4 * 10));
        } else if (gamepad1.y) {
            driver.drive = common.drive.distance((int) (25.4 * 20));
        } else if (gamepad1.b) {
            liftReady = false;
            liftState = LIFT_STATE.INIT;
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