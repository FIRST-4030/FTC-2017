package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.buttons.BUTTON_TYPE;
import org.firstinspires.ftc.teamcode.buttons.PAD_BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.robot.common.Common;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "TeleOp")
public class TeleOpMode extends OpMode {

    // Drive speeds
    private final static float SCALE_FULL = 1.0f;
    private final static float SCALE_SLOW = SCALE_FULL * 0.5f;

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
        //buttons.register("CLAW-" + CLAWS.TOP, gamepad2, PAD_BUTTON.right_bumper);
        //buttons.register("CLAW-" + CLAWS.BOTTOM, gamepad2, PAD_BUTTON.left_bumper);
        buttons.register("EXTEND-INTAKE", gamepad2, PAD_BUTTON.b);
        buttons.register("SLOW-MODE", gamepad1, PAD_BUTTON.a, BUTTON_TYPE.TOGGLE);

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
        liftSystem();

        // Driver Feedback
        telemetry.addData("Wheels", robot.wheels.isAvailable());
        telemetry.addData("Teleop", robot.wheels.isTeleop());
        telemetry.addData("Slow Mode", buttons.get("SLOW-MODE"));
        //telemetry.addData("Top CLaw", robot.claws[CLAWS.TOP.ordinal()].getPostion());
        //telemetry.addData("Bottom Claw", robot.claws[CLAWS.BOTTOM.ordinal()].getPostion());
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

    public void liftSystem() {

        // Lift
       float liftPower = gamepad2.right_trigger - gamepad2.left_trigger;
       robot.lift.setPower(liftPower);

        // Intakes
        // TODO: Map the lift to something else so we can use both sticks
        robot.intakes[INTAKES.LEFT.ordinal()].setPower(-gamepad2.right_stick_y * 1.1f);
        robot.intakes[INTAKES.RIGHT.ordinal()].setPower(-gamepad2.right_stick_y * 0.9f);

        // Claws
//        for (CLAWS claw : CLAWS.values()) {
//            if (buttons.get("CLAW-" + claw)) {
//                robot.claws[claw.ordinal()].toggle();
//                telemetry.addData("CLAW-" + claw, robot.claws[claw.ordinal()].getPostion());
//            }
//        }

        //intake motors
        robot.intakes[INTAKES.LEFT.ordinal()].setPower(gamepad2.left_stick_y);
        robot.intakes[INTAKES.RIGHT.ordinal()].setPower(gamepad2.right_stick_y);
    }
}
