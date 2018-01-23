package org.firstinspires.ftc.teamcode.robot.calibration;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.buttons.BUTTON_TYPE;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.buttons.PAD_BUTTON;
import org.firstinspires.ftc.teamcode.calibration.Subsystem;
import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.field.Field;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.robot.common.Drive;
import org.firstinspires.ftc.teamcode.utils.Heading;
import org.firstinspires.ftc.teamcode.utils.Round;

public class Distance extends Subsystem {
    private static final String P = "DRIVE_P";
    private static final String I = "DRIVE_I";
    private static final String INCREMENT = "DRIVE-INCREMENT";
    private static final float MIN_INCREMENT = 0.001f;
    private static final float MAX_INCREMENT = 0.1f;

    private static final String JOYSTICK = "JOYSTICK";
    private static final String TINY = "TINY";
    private static final String SMALL = "SMALL";
    private static final String LARGE = "LARGE";
    private static final String HUGE = "HUGE";
    private static final String BACK = "BACK";

    private AutoDriver driver = new AutoDriver();

    public Distance(OpMode opmode, Robot robot, ButtonHandler buttons) {
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

        buttons.register(TINY, opmode.gamepad1, PAD_BUTTON.a);
        buttons.register(SMALL, opmode.gamepad1, PAD_BUTTON.x);
        buttons.register(LARGE, opmode.gamepad1, PAD_BUTTON.b);
        buttons.register(HUGE, opmode.gamepad1, PAD_BUTTON.y);
        buttons.register(BACK, opmode.gamepad1, PAD_BUTTON.back, BUTTON_TYPE.TOGGLE);
        buttons.register(JOYSTICK, opmode.gamepad1, PAD_BUTTON.start, BUTTON_TYPE.TOGGLE);
    }

    protected void unload() {
        buttons.spinners.remove(P);
        buttons.spinners.remove(I);
        buttons.spinners.remove(INCREMENT);

        buttons.deregister(TINY);
        buttons.deregister(SMALL);
        buttons.deregister(LARGE);
        buttons.deregister(HUGE);
        buttons.deregister(BACK);
    }

    protected void update() {
        Drive.DRIVE_PARAMS.P = buttons.spinners.getFloat(P);
        Drive.DRIVE_PARAMS.I = buttons.spinners.getFloat(I);

        robot.telemetry.addData("Encoder", Round.truncate(robot.wheels.getEncoder()));

        // Handle AutoDriver driving
        driver = robot.common.drive.loop(driver);
        if (driver.isRunning(opmode.time)) {
            return;
        }

        // Allow joystick driving
        if (buttons.get(JOYSTICK)) {
            robot.wheels.loop(opmode.gamepad1);
        }

        // Allow driving backwards in auto
        float scale = Field.MM_PER_INCH;
        if (buttons.get(BACK)) {
            scale *= -1.0f;
        }

        // Process new AutoDriver commands
        if (buttons.get(HUGE)) {
            driver.drive = robot.common.drive.distance((int) (96 * scale));
        } else if (buttons.get(LARGE)) {
            driver.drive = robot.common.drive.distance((int) (48 * scale));
        } else if (buttons.get(SMALL)) {
            driver.drive = robot.common.drive.distance((int) (24 * scale));
        } else if (buttons.get(TINY)) {
            driver.drive = robot.common.drive.distance((int) (12 * scale));
        }
    }
}
