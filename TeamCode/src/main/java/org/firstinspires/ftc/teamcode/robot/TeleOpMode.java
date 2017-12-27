package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.buttons.BUTTON_TYPE;
import org.firstinspires.ftc.teamcode.buttons.BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.robot.common.Common;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "TeleOp")
public class TeleOpMode extends OpMode {

    // Drive speeds
    private final static double SCALE_FULL = 1.0d;
    private final static double SCALE_SLOW = SCALE_FULL * 0.5d;

    // Devices and subsystems
    private Robot robot = null;
    private ButtonHandler buttons;

    @Override
    public void init() {

        // Placate drivers
        telemetry.addData(">", "Initializing...");
        telemetry.update();

        // Init the common tasks elements
        robot = new Robot(hardwareMap, telemetry);

        // Register buttons
        buttons = new ButtonHandler(robot);
        buttons.register("CLAW-" + CLAWS.TOP, gamepad2, BUTTON.right_bumper);
        buttons.register("CLAW-" + CLAWS.BOTTOM, gamepad2, BUTTON.left_bumper);
        buttons.register("EXTEND-INTAKE", gamepad2, BUTTON.b);
        buttons.register("SLOW-MODE", gamepad1, BUTTON.a, BUTTON_TYPE.TOGGLE);

        // Wait for the game to begin
        telemetry.addData(">", "Init complete");
        telemetry.update();

    }

    @Override
    public void start() {
        robot.wheels.setTeleop(true);
        robot.jewelArm.setPosition(Common.JEWEL_ARM_RETRACT);
    }

    @Override
    public void loop() {

        // Update buttons
        buttons.update();

        // Move the robot
        driveBase();
        clawsAndLift();

        // Driver Feedback
        telemetry.addData("Wheels", robot.wheels.isAvailable());
        telemetry.addData("Teleop", robot.wheels.isTeleop());
        telemetry.addData("Slow Mode", buttons.get("SLOW-MODE"));
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
                telemetry.addData("CLAW-" + claw, robot.claws[claw.ordinal()].getPostion());
            }
        }
    }
}
