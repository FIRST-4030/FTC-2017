package org.firstinspires.ftc.teamcode.wheels;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Created by robotics on 11/24/2017.
 */

public class MechanumDrive extends TankDrive implements Wheels {


    public MechanumDrive(HardwareMap map, Telemetry telemetry, WheelsConfig config) {
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

    public void setSpeed(double speed, double angle) {

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

    @Override
    public void loop(Gamepad pad) {

        //TODO: update for mechanum

    }

}
