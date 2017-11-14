package org.firstinspires.ftc.teamcode.westCoast;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.*;

import org.firstinspires.ftc.teamcode.buttons.BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;

/**
 * Created by Alex and Bryan on 10/9/2017.
 *
 * abstract class to provide west coast movement methods.
 *
 */

public abstract class WestCoastOpMode extends OpMode{

    public final int LIFT_RANGE = 100; //test value - to be changed

    public final int LEFT = 0;
    public final int RIGHT = 1;

    public final int TOP_CLAW = 0;
    public final int BOTTOM_CLAW = 1;

    public final double UPPER_CLAW_MAX = .48;
    public final double UPPER_CLAW_MIN = .06;
    public final double LOWER_CLAW_MAX = .35;
    public final double LOWER_CLAW_MIN = .09;
    public final double BUMPER_SERVO_MIN = .8;
    public final double BUMPER_SERVO_MAX = 1;

    public DcMotor lWheel1;
    public DcMotor lWheel2;
    public DcMotor rWheel1;
    public DcMotor rWheel2;
    public DcMotor lift;
    public DcMotor lBumperM;
    public DcMotor rBumperM;
    public Servo lBumperS;
    public Servo rBumperS;
    public Servo topClaw;
    public Servo bottomClaw;
    public AnalogInput liftSwitch;

    public int liftMinimum;

    //button abstraction stuff
    public ButtonHandler buttons = new ButtonHandler();

    public void init() {
        // Set Up hardware
        lWheel1 = hardwareMap.dcMotor.get("ML1");
        lWheel2 = hardwareMap.dcMotor.get("ML2");
        rWheel1 = hardwareMap.dcMotor.get("MR1");
        rWheel2 = hardwareMap.dcMotor.get("MR2");
        lift = hardwareMap.dcMotor.get("LM1");
        topClaw = hardwareMap.servo.get("CL1");
        bottomClaw = hardwareMap.servo.get("CL2");
        //liftSwitch = hardwareMap.analogInput.get("LS1");
        lBumperM = hardwareMap.dcMotor.get("lBumperM");
        rBumperM = hardwareMap.dcMotor.get("rBumperM");
        lBumperS = hardwareMap.servo.get("lBumperS");
        rBumperS = hardwareMap.servo.get("rBumperS");


        topClaw.setDirection(Servo.Direction.REVERSE);
//        lBumperS.setDirection(Servo.Direction.REVERSE);
//        rBumperS.setDirection(Servo.Direction.REVERSE);


        //register buttons
        buttons.register("INTAKE-PRESSED", gamepad2, BUTTON.a);
        buttons.register("INTAKE-IN", gamepad2, BUTTON.dpad_down);
        buttons.register("INTAKE-OUT", gamepad2, BUTTON.dpad_up);

        //while(!liftSwitchIsPressed()){
        //    lift.setPower(1); // POSITIVE IS DOWN!!!
        //}

        //liftMinimum = getLiftPosition();
        //lift.setPower(0);

        //intialize servos.
//        setServoPosition(TOP_CLAW, UPPER_CLAW_MIN);
//        setServoPosition(BOTTOM_CLAW, LOWER_CLAW_MIN);

        //initialize bumper servos to be in and set limits
        lBumperS.scaleRange(BUMPER_SERVO_MIN, BUMPER_SERVO_MAX);
        rBumperS.scaleRange(BUMPER_SERVO_MIN, BUMPER_SERVO_MAX);
        lBumperS.setPosition(BUMPER_SERVO_MIN);
        rBumperS.setPosition(BUMPER_SERVO_MIN);

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

    public void setLiftPower(DcMotor liftMotor, double power){
        if (Math.abs(power) > 1) {
            throw new IllegalArgumentException("liftMotor: the power value must be between -1 and 1 inclusive");
        }

        boolean tryingToGoBelow = getLiftPosition() >= liftMinimum && power < 0;
        boolean tryingToGoAbove = getLiftPosition() <= (liftMinimum + LIFT_RANGE) && power > 0;

        if(!tryingToGoBelow || !tryingToGoAbove)
            liftMotor.setPower(power);
        else liftMotor.setPower(0);
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

    public boolean liftSwitchIsPressed(){
        return liftSwitch.getVoltage() < .5;
    }

    public int getLiftPosition(){
        return lift.getCurrentPosition();
    }

}
