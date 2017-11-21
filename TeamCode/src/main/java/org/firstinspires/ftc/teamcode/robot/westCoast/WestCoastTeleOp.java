package org.firstinspires.ftc.teamcode.robot.westCoast;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.buttons.BUTTON_TYPE;
import org.firstinspires.ftc.teamcode.robot.auto.CommonTasks;
import org.firstinspires.ftc.teamcode.buttons.BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.robot.CLAWS;
import org.firstinspires.ftc.teamcode.robot.Robot;

@TeleOp(name = "The Real WCTeleOp", group = "WestCoastOpMode")
public class WestCoastTeleOp extends OpMode {

    // Drive speeds
    private final static double SCALE_FULL = 1.0d;
    private final static double SCALE_SLOW = SCALE_FULL * 0.5d;

    // Devices and subsystems
    private Robot robot = null;
    private ButtonHandler buttons = new ButtonHandler();

    @Override
    public void init() {

        // Placate drivers
        telemetry.addData(">", "Initializing...");
        telemetry.update();

        // Init the common tasks elements
        robot = new Robot(hardwareMap, telemetry);

        // Register buttons
        buttons.register("TOP-CLAW", gamepad2, BUTTON.right_bumper);
        buttons.register("BOTTOM-CLAW", gamepad2, BUTTON.left_bumper);
        buttons.register("EXTEND-INTAKE", gamepad2, BUTTON.b);
        buttons.register("LOCK-INTAKE", gamepad2, BUTTON.a, BUTTON_TYPE.TOGGLE);
        buttons.register("SLOW-MODE", gamepad1, BUTTON.a, BUTTON_TYPE.TOGGLE);

        // Wait for the game to begin
        telemetry.addData(">", "Init complete");
        telemetry.update();

    }

    @Override
    public void loop() {

        // Update buttons
        buttons.update();

        // Move the robot
        driveBase();
        clawsAndLift();
        intakes();

        // Driver Feedback
        telemetry.addData("Slow Mode", buttons.get("SLOW-MODE"));
        telemetry.addData("Intakes Locked", buttons.get("LOCK-INTAKE"));
        telemetry.addData("Top CLaw", robot.claws[CLAWS.TOP.ordinal()].getPostion());
        telemetry.addData("Bottom Claw", robot.claws[CLAWS.BOTTOM.ordinal()].getPostion());
        telemetry.addData("Lift Height", robot.lift.getEncoder());
        telemetry.update();
    }

    public void driveBase() {
        if (buttons.get("SLOW-MODE")) {
            robot.wheels.setSpeedScale(SCALE_SLOW);
        } else {
            robot.wheels.setSpeedScale(SCALE_FULL);
        }
        robot.wheels.loop(gamepad1);
    }

    public void clawsAndLift() {

        // Lift
        robot.lift.setPower(gamepad2.left_stick_y);

        // Claws
        for (CLAWS claw : CLAWS.values()) {
            if (buttons.get("CLAW-" + claw)) {
                robot.claws[claw.ordinal()].toggle();
            }
        }
    }

    public void intakes() {

        // Toggle Intakes
        if (buttons.get("EXTEND-INTAKE")) {
            for (ServoFTC intake : robot.intakeArms) {
                intake.toggle();
            }
        }

        // Intake Motors
        if (!buttons.get("LOCK-INTAKE")) {
            for (Motor intake : robot.intakes) {
                intake.setPower(gamepad2.right_stick_y);
            }
        }
    }
}
