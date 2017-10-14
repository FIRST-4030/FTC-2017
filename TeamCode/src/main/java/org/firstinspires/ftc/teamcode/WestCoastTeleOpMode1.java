package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
/**
 * Created by Alex Wang on 10/9/2017.
 */

@TeleOp(name="WestCoastTeleOpMode1", group="WestCoastOpMode")
public class WestCoastTeleOpMode1 extends WestCoastOpMode{

    @Override
    public void loop()
    {

        moveBase(); // change to analogue

        moveClaws();

        moveLift();

        telemetry.addData("Top:", topClaw.getPosition());
        telemetry.addData("Bottom:", bottomClaw.getPosition());
        telemetry.update();

    }

    public void moveLift(){

        liftMotor(lift, gamepad2.left_stick_y);

    }

    public void moveClaws(){

        if(!gamepad2.right_bumper){

            setServoPosition(TOP_CLAW, gamepad2.right_trigger);

        }

        if(!gamepad2.left_bumper){

            setServoPosition(BOTTOM_CLAW, gamepad2.left_trigger);

        }

//        if(gamepad2.a) setServoPosition(bottomClaw, LOWER_CLAW_MIN);
//        else if(gamepad2.b) setServoPosition(bottomClaw, LOWER_CLAW_MAX);
//
//        if(gamepad2.x) setServoPosition(topClaw, UPPER_CLAW_MIN);
//        else if(gamepad2.y) setServoPosition(topClaw, UPPER_CLAW_MAX);

    }

    public void moveBase(){

        // Tank drive
        setSidePower(LEFT, gamepad1.left_stick_y);
        setSidePower(RIGHT, gamepad1.right_stick_y);

//        if(gamepad1.dpad_down){
//
//            setSidePower(RIGHT, -1);
//            setSidePower(LEFT, -1);
//
//        } else if(gamepad1.dpad_right){
//
//            setSidePower(RIGHT, -1);
//            setSidePower(LEFT, 1);
//
//        } else if(gamepad1.dpad_up){
//
//            setSidePower(RIGHT, 1);
//            setSidePower(LEFT, 1);
//
//        } else if(gamepad1.dpad_left){
//
//            setSidePower(RIGHT, 1);
//            setSidePower(LEFT, -1);
//
//        } else {
//
//            setSidePower(RIGHT, 0);
//            setSidePower(LEFT, 0);
//
//        }

    }

}
