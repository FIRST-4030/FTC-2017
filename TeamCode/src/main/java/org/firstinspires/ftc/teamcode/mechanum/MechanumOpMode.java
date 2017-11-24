package org.firstinspires.ftc.teamcode.mechanum;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.actuators.ServoFTCConfig;
import org.firstinspires.ftc.teamcode.buttons.BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.buttons.SinglePressButton;

import static org.firstinspires.ftc.teamcode.buttons.BUTTON_TYPE.TOGGLE;

/**
 * Created by robotics on 9/16/2017.
 *
 * by Bryan Cook
 *
 * An OpMode abstract class
 *
 */

public abstract class MechanumOpMode extends OpMode {

    public DcMotor FLWheel;
    public DcMotor FRWheel;
    public DcMotor BLWheel;
    public DcMotor BRWheel;

    public ServoFTCConfig jewelArm = new ServoFTCConfig("JewelArm", false, 0d, .5d);
    public ServoFTC jewelServo = new ServoFTC(hardwareMap, jewelArm, telemetry);

    double fLVM;
    double fRVM;
    double bLVM;
    double bRVM;

    private ButtonHandler button = new ButtonHandler();



    @Override
    /*
      Make sure to call `super.init()` the first line of the init() method when you extend this
      class
     */
    public void init() {

        // NOTE: THE BACK LEFT AND BACK RIGHT WHEELS ARE LABELED WRONG ON THE MECHANUM CONFIGURATION!
        // TO COMPENSATE, THE BACK WHEELS ARE SWITCHED IN THIS PIECE OF CODE, TO AVOID SCREWING WITH EXISTING CODE.

        // NOW the front too?

        FLWheel = hardwareMap.dcMotor.get("FR");
        FRWheel = hardwareMap.dcMotor.get("FL");
        BLWheel = hardwareMap.dcMotor.get("BR");
        BRWheel = hardwareMap.dcMotor.get("BL");

        //register button
        button.register("Jewel-Whacker", gamepad2, BUTTON.a, TOGGLE);
    }

    /**
     * Turn a DC motor at a power range of -1 to 1
     *
     * Throws an IllegalArgumentException if the power parameter is not between -1 and 1 inclusive
     *
     * @param motor The motor to turn
     * @param power The power to turn the motor at. Between -1 and 1
     */
    public void turnMotor(DcMotor motor, double power){
        if(Math.abs(power) > 1) throw new IllegalArgumentException("turnMotor: The motor power must be between -1 and 1");

        motor.setPower(power);

    }


    /**
     * Move the mechanum drive base given a move speed, a move angle, and a rotation speed
     *
     * Will throw an Illegal argument exception if the speed or rotationSpeed are not between -1
     * and 1 inclusive
     *
     * @param moveAngle The direction of the robot's desired velocity vector.
     *                  (pi is straight ahead?)
     * @param speed The magnitude of the robot's desired velocity vector. Between -1 and 1
     * @param rotationSpeed The desired Rotation speed of the robot. Between -1 and 1.
     */
    public void move(double moveAngle, double speed, double rotationSpeed){

        if(Math.abs(speed) > 1) throw new IllegalArgumentException(
                "move: The speed parameter must be between 1 and -1 inclusive");
        if(Math.abs(rotationSpeed) > 1) throw new IllegalArgumentException(
                "move: The rotationSpeed parameter must be between 1 and -1 inclusive");

        /*

        voltage multiplier = vm

        FL vm = (speed)(sin(moveAngle + (pi / 4))) + rotationSpeed
        FR vm = (speed)(cos(moveAngle + (pi / 4))) - rotationSpeed
        BL vm = (speed)(cos(moveAngle + (pi / 4))) + rotationSpeed
        BR vm = (speed)(sin(moveAngle + (pi / 4))) - rotationSpeed

        vm is between -2 and 2 - needs to be between 1 and -1
        DON'T JUST DIVIDE BY 2 (?????)
        DON'T JUST USE A PIECEWISE FUNCTION

        Find the largest |vm| of the 4 motors
        divide each vm by the largest

         */

        // Calculate the VMs
        fLVM = (speed * Math.sin(moveAngle + (Math.PI / 4))) - rotationSpeed;
        fRVM = (speed * Math.cos(moveAngle + (Math.PI / 4))) + rotationSpeed;
        bLVM = (speed * Math.cos(moveAngle + (Math.PI / 4))) - rotationSpeed;
        bRVM = (speed * Math.sin(moveAngle + (Math.PI / 4))) + rotationSpeed;

        // Account for the range of the VMs
//        double largestVM = Math.abs(fLVM);
//        if(Math.abs(fRVM) > largestVM) largestVM = Math.abs(fRVM);
//        if(Math.abs(bLVM) > largestVM) largestVM = Math.abs(bLVM);
//        if(Math.abs(bRVM) > largestVM) largestVM = Math.abs(bRVM);
//
//        if(largestVM != 0) {
//            fLVM = fLVM / largestVM;
//            fRVM = fRVM / largestVM;
//            bLVM = bLVM / largestVM;
//            bRVM = bRVM / largestVM;
//        }

        //temporary thingy that allows for slower speeds
        //fLVM = fLVM / 2;
        //fRVM = fRVM / 2;
        //bLVM = bLVM / 2;
        //bRVM = bRVM / 2;

        telemetry.addData("FL Speed", fLVM);
        telemetry.addData("FR Speed", fRVM);
        telemetry.addData("BL Speed", bLVM);
        telemetry.addData("BR Speed", bRVM);
        telemetry.addData("netSpeed", speed);
        telemetry.addData("moveAngle", moveAngle);
        telemetry.addData("rotationSpeed", rotationSpeed);

        // Use the VMs as power values for the motors
        turnMotor(FLWheel, fLVM);
        turnMotor(FRWheel, -fRVM);
        turnMotor(BLWheel, bLVM);
        turnMotor(BRWheel, -bRVM);


    }

    // Simple methods to move cardinal directions - less complicated but also less powerful than
    // the moveRobot(...) method

    /**
     * A much simpler but less flexible version of the move() method
     *
     * Will throw an Illegal Argument Exception if the value of speed in not between -1 and 1
     * inclusive
     *
     * @param direction 1 of 4 directions to move in
     * @param speed The desired robot speed. Between -1 and 1
     */
    public void simpleMove(Direction direction, double speed){

        if(Math.abs(speed) > 1) throw new IllegalArgumentException(
                "simpleMove: The parameter speed must be between -1 and 1 inclusive");

        move(direction.getMoveDirection(), speed, 0);

    }
    
    /**
     * Stop moving the robot
     */
    public void stop(){
        FLWheel.setPower(0);
        FRWheel.setPower(0);
        BLWheel.setPower(0);
        BRWheel.setPower(0);
    }

}
