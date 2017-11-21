package org.firstinspires.ftc.teamcode.westCoast;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.auto.CommonTasks;
import org.firstinspires.ftc.teamcode.buttons.BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.robot.CLAWS;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.wheels.MotorSide;
import org.firstinspires.ftc.teamcode.wheels.TankDrive;

/**
 * Created by bnstc on 11/18/2017.
 * <p>
 * Note that the
 */


@TeleOp(name = "The Real WCTeleOp", group = "WestCoastOpMode")
public class WestCoastTeleOp extends OpMode {

    // Devices and subsystems
    private Robot robot = null;
    private CommonTasks common = null;

    private ButtonHandler buttons = new ButtonHandler();

    private boolean topClawOpen = true;
    private boolean bottomClawOpen = true;
    private boolean intakeExtended = false;
    private boolean intakeLocked = true;
    private boolean slowMode = false;

    @Override
    public void init() {

        // Placate drivers
        telemetry.addData(">", "Initializing...");
        telemetry.update();

        // Init the common tasks elements in CALIBRATION mode
        robot = new Robot(hardwareMap, telemetry);
        common = new CommonTasks(robot);

        // Register buttons
        buttons.register("TOP-CLAW", gamepad2, BUTTON.right_bumper);
        buttons.register("BOTTOM-CLAW", gamepad2, BUTTON.left_bumper);
        buttons.register("EXTEND-INTAKE", gamepad2, BUTTON.b);
        buttons.register("LOCK-INTAKE", gamepad2, BUTTON.a);
        buttons.register("SLOW-MODE", gamepad1, BUTTON.a);

        // Wait for the game to begin
        telemetry.addData(">", "Init complete");
        telemetry.update();

    }

    @Override
    public void loop() {

        // Update Buttons
        buttons.update();

        driveBase();

        clawsAndLift();

        intakes();

        // Driver Feedback
        telemetry.addData("Slow Mode", slowMode);
        telemetry.addData("Intakes Locked", intakeLocked);
        telemetry.addData("Top CLaw", robot.claws[CLAWS.TOP.ordinal()].getPostion());
        telemetry.addData("Bottom Claw", robot.claws[CLAWS.BOTTOM.ordinal()].getPostion());
        telemetry.addData("Lift Height", robot.lift.getEncoder());

    }

    public void driveBase() {

        // Update Slow Mode
        if (buttons.get("SLOW-MODE")) slowMode = !slowMode;

        // Tank Drive
        if (slowMode) {
            robot.tank.setSpeedScale(0.5);
        } else {
            robot.tank.setSpeedScale(1.0);
        }
        robot.tank.loop(gamepad1);
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

        // Lock Intakes
        if (buttons.get("LOCK-INTAKE")) intakeLocked = !intakeLocked;

        // Toggle Intakes
        if (buttons.get("EXTEND-INTAKE")) {
            intakeExtended = !intakeExtended;
            for (ServoFTC intake : robot.intakeArms) {
                if (intakeExtended) intake.min();
                else intake.max();
            }
        }

        // Intake Motors
        if (!intakeLocked) {
            for (Motor intake : robot.intakes) {
                intake.setPower(gamepad2.right_stick_y);
            }
        }
    }
}
