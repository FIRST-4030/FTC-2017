package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
/**
 * Created by Alex Wang on 10/9/2017.
 */

@TeleOp(name="WestCoastTeleOpMode1", group="WestCoastOpMode")
public class WestCoastTeleOpMode1 extends WestCoastOpMode{

    public boolean topClawOpen = false;
    public boolean bottomClawOpen = false;

    @Override
    public void loop()
    {

        moveBase(); // change to analogue

        moveClaws();

        moveLift();

        telemetry.addData("Top Claw", topClaw.getPosition());
        telemetry.addData("Bottom Claw", bottomClaw.getPosition());
        telemetry.addData("Lift Position", lift.getCurrentPosition());
        telemetry.update();

    }

    public void moveLift(){

        setLiftPower(lift, gamepad2.left_stick_y);

    }

    public void moveClaws(){

        if(gamepad2.right_bumper){

            setServoPosition(TOP_CLAW, (topClawOpen ? UPPER_CLAW_MIN : UPPER_CLAW_MAX));

            topClawOpen = !topClawOpen;

        }

        if(gamepad2.left_bumper){

            setServoPosition(BOTTOM_CLAW, (bottomClawOpen ? LOWER_CLAW_MIN : LOWER_CLAW_MAX));

            bottomClawOpen = !bottomClawOpen;

        }

    }

    public void moveBase(){

        // Tank drive
        setSidePower(LEFT, -gamepad1.left_stick_y);
        setSidePower(RIGHT, -gamepad1.right_stick_y);

    }

}
