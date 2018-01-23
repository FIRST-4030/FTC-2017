package org.firstinspires.ftc.teamcode.robot.calibration;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.buttons.PAD_BUTTON;
import org.firstinspires.ftc.teamcode.calibration.Subsystem;
import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.robot.common.Drive;
import org.firstinspires.ftc.teamcode.utils.Heading;
import org.firstinspires.ftc.teamcode.utils.Round;

public class Turns extends Subsystem {
    private static final String P = "TURN_P";
    private static final String I = "TURN_I";
    private static final String INCREMENT = "TURN-INCREMENT";
    private static final float MIN_INCREMENT = 0.001f;
    private static final float MAX_INCREMENT = 0.1f;

    private static final String JOYSTICK = "JOYSTICK";
    private static final String CW = "PLUS_90";
    private static final String CCW = "MINUS_90";
    private static final String ANGLE_0 = "0";
    private static final String ANGLE_90 = "90";
    private static final String ANGLE_180 = "180";
    private static final String ANGLE_270 = "270";
    private AutoDriver driver = new AutoDriver();

    public Turns(OpMode opmode, Robot robot, ButtonHandler buttons) {
        super(opmode, robot, buttons);
    }

    public String name() {
        return this.getClass().getSimpleName();
    }

    protected void load() {
        buttons.spinners.add(INCREMENT,
                opmode.gamepad1, PAD_BUTTON.right_bumper, PAD_BUTTON.left_bumper,
                Round.magnitudeValue(Drive.TURN_PARAMS.P / 100.0d),
                Round.magnitudeValue(Drive.TURN_PARAMS.P / 10.0d));
        buttons.spinners.setLimit(INCREMENT, MIN_INCREMENT, false);
        buttons.spinners.setLimit(INCREMENT, MAX_INCREMENT, true);

        buttons.spinners.add(P,
                opmode.gamepad1, PAD_BUTTON.dpad_up, PAD_BUTTON.dpad_down,
                INCREMENT, Drive.TURN_PARAMS.P);
        buttons.spinners.add(I,
                opmode.gamepad1, PAD_BUTTON.dpad_right, PAD_BUTTON.dpad_left,
                INCREMENT, Drive.TURN_PARAMS.I);

        buttons.register(JOYSTICK, opmode.gamepad1, PAD_BUTTON.right_trigger);
        buttons.register(CCW, opmode.gamepad1, PAD_BUTTON.left_stick_button);
        buttons.register(CW, opmode.gamepad1, PAD_BUTTON.right_stick_button);
        buttons.register(ANGLE_0, opmode.gamepad2, PAD_BUTTON.dpad_up);
        buttons.register(ANGLE_90, opmode.gamepad2, PAD_BUTTON.dpad_right);
        buttons.register(ANGLE_180, opmode.gamepad2, PAD_BUTTON.dpad_down);
        buttons.register(ANGLE_270, opmode.gamepad2, PAD_BUTTON.dpad_left);
    }

    protected void unload() {
        buttons.spinners.remove(P);
        buttons.spinners.remove(I);
        buttons.spinners.remove(INCREMENT);

        buttons.deregister(JOYSTICK);
        buttons.deregister(CCW);
        buttons.deregister(CW);
        buttons.deregister(ANGLE_0);
        buttons.deregister(ANGLE_90);
        buttons.deregister(ANGLE_180);
        buttons.deregister(ANGLE_270);
    }

    protected void update() {
        Drive.TURN_PARAMS.P = buttons.spinners.getFloat(P);
        Drive.TURN_PARAMS.I = buttons.spinners.getFloat(I);
        double angle = Math.toDegrees(Math.atan2(opmode.gamepad1.left_stick_y, opmode.gamepad1.left_stick_x));

        robot.telemetry.addData("Joystick", Round.truncate(angle));
        robot.telemetry.addData("Gyro", robot.gyro.isReady() ? Round.truncate(robot.gyro.getHeading()) : "<Calibrating>");

        // Handle AutoDriver driving
        driver = robot.common.drive.loop(driver);
        if (driver.isRunning(opmode.time)) {
            return;
        }

        // Process new AutoDriver commands
        if (buttons.get(JOYSTICK)) {
            driver.drive = robot.common.drive.heading((float) angle);
        } else if (buttons.get(CCW)) {
            driver.drive = robot.common.drive.heading(Heading.normalize(robot.gyro.getHeading() - 90));
        } else if (buttons.get(CW)) {
            driver.drive = robot.common.drive.heading(Heading.normalize(robot.gyro.getHeading() + 90));
        } else if (buttons.get(ANGLE_0)) {
            driver.drive = robot.common.drive.heading(0);
        } else if (buttons.get(ANGLE_90)) {
            driver.drive = robot.common.drive.heading(90);
        } else if (buttons.get(ANGLE_180)) {
            driver.drive = robot.common.drive.heading(180);
        } else if (buttons.get(ANGLE_270)) {
            driver.drive = robot.common.drive.heading(270);
        }
    }
}
