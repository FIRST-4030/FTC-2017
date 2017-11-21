package org.firstinspires.ftc.teamcode.robot.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.robot.auto.CommonTasks;
import org.firstinspires.ftc.teamcode.buttons.BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.robot.CLAWS;
import org.firstinspires.ftc.teamcode.robot.INTAKES;
import org.firstinspires.ftc.teamcode.robot.Robot;

@TeleOp(name = "Calibration", group = "Test")
public class Calibration extends OpMode {

    private static final double SERVO_INTERVAL_INTERVAL = 0.01d;

    // Devices and subsystems
    private Robot robot = null;
    private CommonTasks common = null;

    // Driving
    private double servoInterval = 0.01;
    private ButtonHandler buttons = new ButtonHandler();

    @Override
    public void init() {

        // Init the common tasks elements in CALIBRATION mode
        robot = new Robot(hardwareMap, telemetry);
        common = new CommonTasks(robot);

        // Register buttons
        buttons.register("CLAW-" + CLAWS.TOP + "-UP", gamepad1, BUTTON.dpad_up);
        buttons.register("CLAW-" + CLAWS.TOP + "-DOWN", gamepad1, BUTTON.dpad_down);
        buttons.register("CLAW-" + CLAWS.BOTTOM + "-UP", gamepad1, BUTTON.dpad_right);
        buttons.register("CLAW-" + CLAWS.BOTTOM + "-DOWN", gamepad1, BUTTON.dpad_left);
        buttons.register("INTAKE-" + INTAKES.RIGHT + "-UP", gamepad1, BUTTON.b);
        buttons.register("INTAKE-" + INTAKES.RIGHT + "-DOWN", gamepad1, BUTTON.a);
        buttons.register("INTAKE-" + INTAKES.LEFT + "-UP", gamepad1, BUTTON.y);
        buttons.register("INTAKE-" + INTAKES.LEFT + "-DOWN", gamepad1, BUTTON.x);
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

        // Adjust the intake servos
        for (INTAKES intake : INTAKES.values()) {
            if (buttons.get("INTAKE-" + intake + "-UP")) {
                robot.intakeArms[intake.ordinal()].setPositionRaw(robot.intakeArms[intake.ordinal()].getPostion() + servoInterval);
            } else if (buttons.get("INTAKE-" + intake + "-DOWN")) {
                robot.intakeArms[intake.ordinal()].setPositionRaw(robot.intakeArms[intake.ordinal()].getPostion() - servoInterval);
            }
        }

        // Adjust the servo adjustment rate
        if (buttons.get("INTERVAL-UP")) {
            servoInterval += SERVO_INTERVAL_INTERVAL;
        }
        if (buttons.get("INTERVAL-DOWN")) {
            servoInterval -= SERVO_INTERVAL_INTERVAL;
        }

        // Feedback
        telemetry.addData("Lift", robot.lift.getEncoder());
        telemetry.addData("Wheels", robot.wheels.getEncoder());
        for (CLAWS claw : CLAWS.values()) {
            telemetry.addData("Claw " + claw, robot.claws[claw.ordinal()].getPostion());
        }
        for (INTAKES intake : INTAKES.values()) {
            telemetry.addData("Intake " + intake, robot.intakeArms[intake.ordinal()].getPostion());
        }
        telemetry.addData("Servo Interval", servoInterval);
        telemetry.update();
    }
}
