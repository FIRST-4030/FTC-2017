package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.auto.CommonTasks;
import org.firstinspires.ftc.teamcode.buttons.BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.config.BOT;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;
import org.firstinspires.ftc.teamcode.wheels.TankDrive;

@TeleOp(name = "Calibration", group = "Test")
public class Calibration extends OpMode {

    private static final double SERVO_INTERVAL_INTERVAL = 0.01d;

    // Devices and subsystems
    private CommonTasks common = null;
    private TankDrive tank = null;
    private ServoFTC[] claws = null;
    private Motor lift = null;

    // Driving
    private double servoInterval = 0.01;
    private ButtonHandler buttons = new ButtonHandler();

    @Override
    public void init() {

        // Init the common tasks elements in CALIBRATION mode
        common = new CommonTasks(hardwareMap, telemetry, BOT.CALIBRATION);
        tank = common.initDrive();
        lift = common.initLift();
        claws = common.initClaws();

        // Register buttons
        buttons.register("CLAW0-UP", gamepad1, BUTTON.dpad_up);
        buttons.register("CLAW0-DOWN", gamepad1, BUTTON.dpad_down);
        buttons.register("CLAW1-UP", gamepad1, BUTTON.dpad_right);
        buttons.register("CLAW1-DOWN", gamepad1, BUTTON.dpad_left);
        buttons.register("INTERVAL-UP", gamepad1, BUTTON.right_stick_button);
        buttons.register("INTERVAL-DOWN", gamepad1, BUTTON.left_stick_button);
    }

    @Override
    public void start() {
        telemetry.clearAll();
        lift.resetEncoder();
        tank.resetEncoder();
    }

    @Override
    public void loop() {

        // Update buttons
        buttons.update();

        // Adjust the lift and wheels
        tank.setSpeed(gamepad1.left_stick_y);
        lift.setPower(gamepad1.right_stick_y);

        // Adjust the claws
        for (int i = 0; i < claws.length; i++) {
            if (buttons.get("CLAW" + i + "-UP")) {
                claws[i].setPosition(claws[i].getPostion() + servoInterval);
            } else if (buttons.get("CLAW" + i + "DOWN")) {
                claws[i].setPosition(claws[i].getPostion() - servoInterval);
            }
        }

        // Adjust the servo adjustment rate
        if (buttons.get("INTERVAL-UP")) {
            servoInterval += SERVO_INTERVAL_INTERVAL;
        } else if (buttons.get("INTERVAL-DOWN")) {
            servoInterval -= SERVO_INTERVAL_INTERVAL;
        }

        // Feedback
        telemetry.addData("Lift", lift.getEncoder());
        telemetry.addData("Wheels", tank.getEncoder());
        for (int i = 0; i < claws.length; i++) {
            telemetry.addData("Claw " + i, claws[i].getPostion());
        }
        telemetry.addData("Servo Interval", servoInterval);
        telemetry.update();
    }
}
