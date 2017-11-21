package org.firstinspires.ftc.teamcode.westCoast;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.auto.CommonTasks;
import org.firstinspires.ftc.teamcode.buttons.BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.config.BOT;
import org.firstinspires.ftc.teamcode.wheels.MotorSide;
import org.firstinspires.ftc.teamcode.wheels.TankDrive;

/**
 * Created by bnstc on 11/18/2017.
 *
 * Note that the
 *
 */


@TeleOp(name = "The Real WCTeleOp", group = "WestCoastOpMode")
public class WestCoastTeleOp extends OpMode {

    // Devices and subsystems
    private CommonTasks common = null;
    private TankDrive tank = null;
    private ServoFTC[] claws = null;
    private Motor lift = null;
    private ServoFTC[] intakeServos = null;
    private Motor[] intakeMotors = null;

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
        common = new CommonTasks(hardwareMap, telemetry);
        tank = common.initDrive();
        lift = common.initLift();
        claws = common.initClaws();
        intakeServos = common.initIntakeServos();
        intakeMotors = common.initIntakeMotors();


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
    public void loop(){

        // Update Buttons
        buttons.update();

        driveBase();

        clawsAndLift();

        intakes();

        // Driver Feedback
        telemetry.addData("Slow Mode", slowMode);
        telemetry.addData("Intakes Locked", intakeLocked);
        telemetry.addData("Top CLaw", claws[CommonTasks.CLAWS.TOP.ordinal()].getPostion());
        telemetry.addData("Bottom Claw", claws[CommonTasks.CLAWS.BOTTOM.ordinal()].getPostion());
        telemetry.addData("Lift Height", lift.getEncoder());

    }

    public void driveBase(){

        // Update Slow Mode
        if(buttons.get("SLOW-MODE")) slowMode = !slowMode;

        // Tank Drive
        tank.setSpeed((-gamepad1.left_stick_y) * (slowMode ? .5 : 1), MotorSide.LEFT);
        tank.setSpeed((-gamepad1.right_stick_y) * (slowMode ? .5 : 1), MotorSide.RIGHT);

    }

    public void clawsAndLift(){

        // Lift
        lift.setPower(gamepad2.left_stick_y);

        // Toggle Claws
        if(buttons.get("TOP-CLAW")){
            topClawOpen = !topClawOpen;
            ServoFTC claw = claws[CommonTasks.CLAWS.TOP.ordinal()];
            if(topClawOpen) claw.min();
            else claw.max();
        }
        if(buttons.get("BOTTOM-CLAW")){
            bottomClawOpen = !bottomClawOpen;
            ServoFTC claw = claws[CommonTasks.CLAWS.BOTTOM.ordinal()];
            if(bottomClawOpen) claw.min();
            else claw.max();
        }

    }

    public void intakes(){

        // Lock Intakes
        if(buttons.get("LOCK-INTAKE")) intakeLocked = !intakeLocked;

        // Toggle Intakes
        if(buttons.get("EXTEND-INTAKE")){
            intakeExtended = !intakeExtended;
            for(ServoFTC intake : intakeServos){
                if(intakeExtended) intake.min();
                else intake.max();
            }
        }

        // Intake Motors
        if(!intakeLocked) {
            for (Motor intake : intakeMotors) {
                intake.setPower(gamepad2.right_stick_y);
            }
        }

    }

}
