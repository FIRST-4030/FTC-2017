package org.firstinspires.ftc.teamcode.robot.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.buttons.BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.robot.CLAWS;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.utils.Round;

@TeleOp(name = "Calibration", group = "Test")
public class Calibration extends OpMode {

    private static final double SERVO_INTERVAL_INTERVAL = 0.01d;

    // Devices and subsystems
    private Robot robot = null;

    // Driving
    private double servoInterval = 0.01;
    private final ButtonHandler buttons = new ButtonHandler();

    @Override
    public void init() {

        // Init the common tasks elements
        robot = new Robot(hardwareMap, telemetry);

        // Put these servos someplace vaguely safe
        for (ServoFTC claw : robot.claws) {
            claw.setPositionRaw(.5);
        }
        robot.jewelArm.setPositionRaw(.5);

        // Register buttons
        buttons.register("CLAW-" + CLAWS.TOP + "-UP", gamepad1, BUTTON.dpad_up);
        buttons.register("CLAW-" + CLAWS.TOP + "-DOWN", gamepad1, BUTTON.dpad_down);
        buttons.register("CLAW-" + CLAWS.BOTTOM + "-UP", gamepad1, BUTTON.dpad_right);
        buttons.register("CLAW-" + CLAWS.BOTTOM + "-DOWN", gamepad1, BUTTON.dpad_left);
        buttons.register("ARM-UP", gamepad1, BUTTON.a);
        buttons.register("ARM-DOWN", gamepad1, BUTTON.b);
        buttons.register("INTERVAL-UP", gamepad1, BUTTON.right_stick_button);
        buttons.register("INTERVAL-DOWN", gamepad1, BUTTON.left_stick_button);
    }

    @Override
    public void start() {
        telemetry.clearAll();
        robot.lift.resetEncoder();
        robot.wheels.resetEncoder();
    }

    @Override
    public void loop() {

        // Update buttons
        buttons.update();

        // Adjust the lift and wheels
        robot.wheels.setSpeed(gamepad1.left_stick_y);
        robot.lift.setPower(gamepad1.right_stick_y);

        // Adjust the claws
        for (CLAWS claw : CLAWS.values()) {
            if (buttons.get("CLAW-" + claw + "-UP")) {
                robot.claws[claw.ordinal()].setPositionRaw(robot.claws[claw.ordinal()].getPostion() + servoInterval);
            } else if (buttons.get("CLAW-" + claw + "-DOWN")) {
                robot.claws[claw.ordinal()].setPositionRaw(robot.claws[claw.ordinal()].getPostion() - servoInterval);
            }
        }

        if (buttons.get("ARM-UP")) {
            robot.jewelArm.setPositionRaw(robot.jewelArm.getPostion() + servoInterval);
        } else if (buttons.get("ARM-DOWN")) {
            robot.jewelArm.setPositionRaw(robot.jewelArm.getPostion() - servoInterval);
        }

        // Adjust the servo adjustment rate
        if (buttons.get("INTERVAL-UP")) {
            servoInterval += SERVO_INTERVAL_INTERVAL;
        }
        if (buttons.get("INTERVAL-DOWN")) {
            servoInterval -= SERVO_INTERVAL_INTERVAL;
        }

        // Feedback
        for (CLAWS claw : CLAWS.values()) {
            telemetry.addData("Claw " + claw, Round.truncate(robot.claws[claw.ordinal()].getPostion()));
        }
        telemetry.addData("Servo Interval", Round.truncate(servoInterval));
        telemetry.addData("Lift", robot.lift.getEncoder());
        telemetry.addData("Lift Switch", robot.liftSwitch.get());
        telemetry.addData("Wheels", robot.wheels.getEncoder());
        telemetry.addData("Arm", Round.truncate(robot.jewelArm.getPostion()));
        telemetry.addData("Gyro", robot.gyro.isReady() ? robot.gyro.getHeading() : "<Not Ready>");
        telemetry.update();
    }
}
