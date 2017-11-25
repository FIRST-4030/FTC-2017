package org.firstinspires.ftc.teamcode.robot.mechanum;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.buttons.BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.config.BOT;
import org.firstinspires.ftc.teamcode.robot.CLAWS;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.robot.auto.CommonTasks;
import org.firstinspires.ftc.teamcode.wheels.TankDrive;

/**
 * Created by robotics on 11/25/2017.
 */

@TeleOp(name="MechanumDriveTest", group="MechanumOpMode")
public class MecanumTeleOp extends OpMode{

    // Devices and subsystems
    private Robot robot = null;

    private ButtonHandler buttons = new ButtonHandler();

    private boolean topClawOpen = true;
    private boolean bottomClawOpen = true;

    @Override
    public void init() {

        // Placate drivers
        telemetry.addData(">", "Initializing...");
        telemetry.update();

        robot = new Robot(hardwareMap, telemetry, BOT.Mecanum);

        // Register buttons
        buttons.register("TOP-CLAW", gamepad2, BUTTON.right_bumper);
        buttons.register("BOTTOM-CLAW", gamepad2, BUTTON.left_bumper);

        // Wait for the game to begin
        telemetry.addData(">", "Init complete");
        telemetry.update();

    }

    @Override
    public void loop() {

        buttons.update();

        robot.wheels.loop(gamepad1);

        robot.lift.setPower(-gamepad2.left_stick_y);

        if(buttons.get("TOP-CLAW")){
            if(topClawOpen) robot.claws[CLAWS.TOP.ordinal()].max();
            else robot.claws[CLAWS.TOP.ordinal()].min();

            topClawOpen = !topClawOpen;
        }

        if(buttons.get("BOTTOM-CLAW")){
            if(bottomClawOpen) robot.claws[CLAWS.BOTTOM.ordinal()].max();
            else robot.claws[CLAWS.BOTTOM.ordinal()].min();

            bottomClawOpen = !bottomClawOpen;
        }

        // Driver Feedback
        telemetry.addData("Top CLaw", robot.claws[CLAWS.TOP.ordinal()].getPostion());
        telemetry.addData("Bottom Claw", robot.claws[CLAWS.BOTTOM.ordinal()].getPostion());
        telemetry.addData("Lift Height", robot.lift.getEncoder());

    }
}
