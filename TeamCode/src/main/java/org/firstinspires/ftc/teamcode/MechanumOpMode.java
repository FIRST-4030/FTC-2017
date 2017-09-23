package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

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



    @Override
    /**
     * Make sure to call `super.init()` the first line of the init() method when you extend this
     * class
     */
    public void init() {
        FLWheel = hardwareMap.dcMotor.get("FL");
        FRWheel = hardwareMap.dcMotor.get("FR");
        BLWheel = hardwareMap.dcMotor.get("BL");
        BRWheel = hardwareMap.dcMotor.get("BR");
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
        if(Math.abs(power) > 1) throw new IllegalArgumentException("The motor power must be between -1 and 1");

        motor.setPower(power);

    }


    /**
     * Move the mechanum drive base given a move speed, a move angle, and a rotation speed
     *
     * Will throw an Illegal argument exception if the speed or rotationSpeed are not betweeen -1
     * and 1 inclusive
     *
     * @param moveAngle The direction of the robot's desired velocity vector.
     *                  (pi is straight ahead?)
     * @param speed The magnitude of the robot's desired velocity vector. Between -1 and 1
     * @param rotationSpeed The desired Rotation speed of the robot. Between -1 and 1.
     */
    public void move(double moveAngle, double speed, double rotationSpeed){

        if(Math.abs(speed) > 1) throw new IllegalArgumentException(
                "The speed parameter must be between 1 and -1 inclusive");
        if(Math.abs(rotationSpeed) > 1) throw new IllegalArgumentException(
                "The rotationSpeed parameter must be between 1 and -1 inclusive");

        /*

        voltage multiplier = vm

        FL vm = (speed)(sin(moveAngle + (pi / 4))) + rotationSpeed
        FR vm = (speed)(cos(moveAngle + (pi / 4))) - rotationSpeed
        BL vm = (speed)(cos(moveAngle + (pi / 4))) + rotationSpeed
        BR vm = (speed)(sin(moveAngle + (pi / 4))) - rotationSpeed

        vm is between -2 and 2 - needs to be between 1 and -1
        DON'T JUST DIVIDE BY 2
        DON'T JUST USE A PIECEWISE FUNCTION

        Find the largest |vm| of the 4 motors
        divide each vm by the largest

         */

        // Calculate the VMs
        double fLVM = (speed * Math.sin(moveAngle + (Math.PI / 4))) + rotationSpeed;
        double fRVM = (speed * Math.cos(moveAngle + (Math.PI / 4))) - rotationSpeed;
        double bLVM = (speed * Math.cos(moveAngle + (Math.PI / 4))) + rotationSpeed;
        double bRVM = (speed * Math.sin(moveAngle + (Math.PI / 4))) - rotationSpeed;

        // Account for the range of the VMs
        double largestVM = Math.abs(fLVM);
        if(Math.abs(fRVM) > largestVM) largestVM = fRVM;
        if(Math.abs(bLVM) > largestVM) largestVM = bLVM;
        if(Math.abs(bRVM) > largestVM) largestVM = bRVM;

        fLVM = fLVM / largestVM;
        fRVM = fRVM / largestVM;
        bLVM = bLVM / largestVM;
        bRVM = bRVM / largestVM;

        // Use the VMs as power values for the motors
        turnMotor(FLWheel, fLVM);
        turnMotor(FRWheel, fRVM);
        turnMotor(BLWheel, bLVM);
        turnMotor(BRWheel, bRVM);


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
                "The parameter speed must be between -1 and 1 inclusive");

        move(direction.moveDirection, speed, 0);

    }

    public void stop(){
        FLWheel.setPower(0);
        FRWheel.setPower(0);
        BLWheel.setPower(0);
        BRWheel.setPower(0);
    }

}
