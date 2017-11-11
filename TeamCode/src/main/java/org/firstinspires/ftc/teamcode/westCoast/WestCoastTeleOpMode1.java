package org.firstinspires.ftc.teamcode.westCoast;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;

/**
 * Created by Alex Wang on 10/9/2017.
 */

@TeleOp(name="WestCoastTeleOpMode1", group="WestCoastOpMode")
public class WestCoastTeleOpMode1 extends WestCoastOpMode{

    public boolean slowMode = false;

    public boolean rBumperPressed = false;
    public boolean lBumperPressed = false;

    //used for spinner on-off switch
    boolean isBumperStickOn = false;


    public boolean topClawOpen = false;
    public boolean bottomClawOpen = false;

    @Override
    public void loop()
    {
        //update the buttons (at this point its only the intake control button)
        buttons.update();

        moveBase(); // change to analogue

        moveClaws();

        moveLift();

        bumpers();

        telemetry.addData("SLOW MODE", slowMode);

        telemetry.addData("Top Claw", topClaw.getPosition());
        telemetry.addData("Bottom Claw", bottomClaw.getPosition());
        // Actually, this IS what we want. The list motor encoder is just bad (sigh)
        telemetry.addData("Lift.getCurrentPosition", getLiftPosition());
        telemetry.addData("switch voltage", liftSwitch.getVoltage());
//        telemetry.addData("switch value", liftSwitch.getValue());
        telemetry.update();

    }

    public void moveLift(){

        setLiftPower(lift, gamepad2.left_stick_y);

    }

    public void moveClaws(){

        if(gamepad2.right_bumper && !rBumperPressed){
            rBumperPressed = true;

            setServoPosition(TOP_CLAW, (topClawOpen ? UPPER_CLAW_MIN : UPPER_CLAW_MAX));
            topClawOpen = !topClawOpen;

        } else if(!gamepad2.right_bumper){
            rBumperPressed = false;
        }

        if(gamepad2.left_bumper && !lBumperPressed){
            lBumperPressed = true;

            setServoPosition(BOTTOM_CLAW, (bottomClawOpen ? LOWER_CLAW_MIN : LOWER_CLAW_MAX));
            bottomClawOpen = !bottomClawOpen;

        } else if(!gamepad2.left_bumper){
            lBumperPressed = false;
        }

//        if(gamepad2.right_bumper){
//
//            setServoPosition(TOP_CLAW, (topClawOpen ? UPPER_CLAW_MIN : UPPER_CLAW_MAX));
//
//            topClawOpen = !topClawOpen;
//
//        }
//
//        if(gamepad2.left_bumper){
//
//            setServoPosition(BOTTOM_CLAW, (bottomClawOpen ? LOWER_CLAW_MIN : LOWER_CLAW_MAX));
//
//            bottomClawOpen = !bottomClawOpen;
//
//        }

    }

    public void moveBase(){

        if(gamepad1.left_bumper) slowMode = true;
        if(gamepad1.right_bumper) slowMode = false;

        // Tank drive
        setSidePower(LEFT, -gamepad1.left_stick_y * (slowMode ? .5 : 1));
        setSidePower(RIGHT, -gamepad1.right_stick_y * (slowMode ? .5 : 1));


    }

    public void bumpers()
    {
        //spinner on-off switch
        if(buttons.get("INTAKE-PRESSED") && isBumperStickOn == false)
        {
            isBumperStickOn = true;
        }
        else if (buttons.get("INTAKE-PRESSED") && isBumperStickOn == true)
        {
            isBumperStickOn = false;
        }

        //set bumper motor power equal to right stick y value if isBumperStickOn = ture
        if(isBumperStickOn)
        {
            lBumperM.setPower(gamepad2.right_stick_y);
            rBumperM.setPower(gamepad2.right_stick_y);
        }

        //retract the servos
        if(buttons.get("INTAKE-IN"))
        {
            lBumperS.setPosition(BUMPER_SERVO_MIN);
            rBumperS.setPosition(BUMPER_SERVO_MIN);
        }

        if(buttons.get("INTAKE-OUT"))
        {
            lBumperS.setPosition(BUMPER_SERVO_MAX);
            rBumperS.setPosition(BUMPER_SERVO_MAX);
        }

    }
}
