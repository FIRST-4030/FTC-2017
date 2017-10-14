package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.*;
/**
 * Created by Alex and Bryan on 10/9/2017.
 *
 * abstract class to provide west coast movement methods.
 *
 */

public abstract class WestCoastOpMode extends OpMode{

    public final int LEFT = 0;
    public final int RIGHT = 1;

    public final int TOP_CLAW = 0;
    public final int BOTTOM_CLAW = 1;

    public final double UPPER_CLAW_MAX = .53;
    public final double UPPER_CLAW_MIN = .03;
    public final double LOWER_CLAW_MAX = .53; // was 1
    public final double LOWER_CLAW_MIN = .03; // was .5

    public DcMotor lWheel1;
    public DcMotor lWheel2;
    public DcMotor rWheel1;
    public DcMotor rWheel2;
    public DcMotor lift;
    public Servo topClaw;
    public Servo bottomClaw;

    public void init() {
        // Set Up hardware
        lWheel1 = hardwareMap.dcMotor.get("ML1");
        lWheel2 = hardwareMap.dcMotor.get("ML2");
        rWheel1 = hardwareMap.dcMotor.get("MR1");
        rWheel2 = hardwareMap.dcMotor.get("MR2");
        lift = hardwareMap.dcMotor.get("LM1");
        topClaw = hardwareMap.servo.get("CL1");
        bottomClaw = hardwareMap.servo.get("CL2");



        topClaw.scaleRange(UPPER_CLAW_MIN, UPPER_CLAW_MAX);
        bottomClaw.scaleRange(LOWER_CLAW_MIN, LOWER_CLAW_MAX);

        topClaw.setDirection(Servo.Direction.REVERSE);
        bottomClaw.setDirection(Servo.Direction.REVERSE);

        setServoPosition(TOP_CLAW, .5);
        setServoPosition(BOTTOM_CLAW, .5);

//        setServoPosition(topClaw, UPPER_CLAW_MIN);
//        setServoPosition(bottomClaw, LOWER_CLAW_MIN);

    }

    /**
     * Turn a DC motor at a power range of -1 to 1
     *
     * Throws an IllegalArgumentException if the power parameter is not between -1 and 1 inclusive
     *
     * @param motor The motor to turn
     * @param power The power to turn the motor at. Between -1 and 1
     */

    public void turnMotor(DcMotor motor, double power) {
        if (Math.abs(power) > 1) {
            throw new IllegalArgumentException("turnmotor: the power value must be between -1 and 1 inclusive");
        }

        motor.setPower(power);

    }

    public void liftMotor(DcMotor liftMotor, double power){
        if (Math.abs(power) > 1) {
            throw new IllegalArgumentException("liftMotor: the power value must be between -1 and 1 inclusive");
        }

        liftMotor.setPower(power);

    }

    public void setServoPosition(int claw, double position){

        if(claw == TOP_CLAW) topClaw.setPosition(Math.max(UPPER_CLAW_MIN, Math.min(position, UPPER_CLAW_MAX)));
        else if(claw == BOTTOM_CLAW) bottomClaw.setPosition(Math.max(LOWER_CLAW_MIN, Math.min(position, LOWER_CLAW_MAX)));
        else throw new IllegalArgumentException("claw must be either 0 (top) or 1 (bottom)");

    }

    public void setSidePower(int side, double power){

        switch(side){
        case LEFT:

            turnMotor(lWheel1, -power);
            turnMotor(lWheel2, -power);

            break;
        case RIGHT:

            turnMotor(rWheel1, power);
            turnMotor(rWheel2, power);

            break;
        default:
            throw new IllegalArgumentException("side must be either 0 (left) or 1 (right)");
        }

    }

}
