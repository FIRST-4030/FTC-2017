package org.firstinspires.ftc.teamcode.wheels;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Created by robotics on 11/24/2017.
 */

public class MecanumDrive extends TankDrive {

    public MecanumDrive(HardwareMap map, Telemetry telemetry, WheelsConfig config) {
        super(map, telemetry, config);
    }

    public void setSpeed(double speed, MOTOR_SIDE side, MOTOR_END end) {
        if (!isAvailable()) {
            return;
        }
        for (WheelMotor motor : config.motors) {
            if (motor.side == side && motor.end == end) {
                motor.motor.setPower(speed * speedScale);
            }
        }
    }

    public void translate(double speed, double angle) {

        // First, change the angle to be pi/4 less than before to account for the diagonal wheels
        angle = angle - Math.PI / 4;
        if (angle < -Math.PI) angle = angle + 2 * Math.PI;

        // THIS ASSUMES THAT THE WHEEL BOTTOMS MAKE A DIAMOND, AND NOT AN X
        // Now the front-right and back-left wheels govern the x-axis speed
        // and the front-left and back-right wheels govern the y-axis speed

        // Figure out the x and y speeds based on the angle and raw speed with trig
        double xSpeed = Math.cos(angle) * speed;
        double ySpeed = Math.sin(angle) * speed;

        // Set the diagonals to the power
        setSpeed(xSpeed, WHEEL_DIAGONAL.FRONT_RIGHT);
        setSpeed(ySpeed, WHEEL_DIAGONAL.FRONT_LEFT);

    }

    public void setSpeed(double speed, WHEEL_DIAGONAL diagonal) {
        if (!isAvailable()) {
            return;
        }

        for (WheelMotor motor : diagonal.getWheels(config)) {
            motor.motor.setPower(speed * speedScale);
        }

    }

    /**
     * Translates the robot, where right is a positive speed, and left is a negative speed.
     * @param speed The speed at which the robot should translate. Positive speed indicates translating to the right.
     */
    public void translate(double speed){
        if(!isAvailable()){
            return;
        }
        setSpeed(-speed, WHEEL_DIAGONAL.FRONT_LEFT);
        setSpeed(speed, WHEEL_DIAGONAL.FRONT_RIGHT);
    }

    @Override
    public void loop(Gamepad pad) {

        // rotation
        if(pad.left_trigger > .05) {
            setSpeed(-pad.left_trigger, MOTOR_SIDE.LEFT);
            setSpeed(pad.left_trigger, MOTOR_SIDE.RIGHT);
        } else if(pad.right_trigger > .05) {
            setSpeed(pad.right_trigger, MOTOR_SIDE.LEFT);
            setSpeed(-pad.right_trigger, MOTOR_SIDE.RIGHT);
        }
        // translating in cardinal directions witht the dpad
        else if(pad.dpad_down) setSpeed(-1);
        else if(pad.dpad_up) setSpeed(1);
        else if(pad.dpad_right) translate(1);
        else if(pad.dpad_left) translate(-1);
        // single wheels
        else if(pad.a) setSpeed(1, MOTOR_SIDE.RIGHT, MOTOR_END.BACK);
        else if(pad.b) setSpeed(1, MOTOR_SIDE.RIGHT, MOTOR_END.FRONT);
        else if(pad.x) setSpeed(1, MOTOR_SIDE.LEFT, MOTOR_END.BACK);
        else if(pad.y) setSpeed(1, MOTOR_SIDE.LEFT, MOTOR_END.FRONT);
        // translating in any direction with the joystick
        else {

            // get clean x and y
            double x = -cleanJoystick(pad.left_stick_x);
            double y = -cleanJoystick(pad.left_stick_y); // Negated so that up is positive

            if(speedFromStick(x, y) > .1) {

                // find the angle
                double angle = Math.atan2(y, x);

                translate(speedFromStick(x, y), angle);

            } else {

                stop();

            }

        }

    }

    /**
     * Accounts for the joystick being able to give a magnitude greater than 1
     *
     * This works best if it's used with the normal signs of X and Y from the stick.
     *
     * @param x The X value of the joystick
     * @param y The Y value of the joystick
     * @return The desired speed, less than or equal to 1
     */
    public double speedFromStick(double x, double y){

        // ummm, my math reduced to this:
        return Math.max(Math.abs(x), Math.abs(y));

    }

}
