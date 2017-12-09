package org.firstinspires.ftc.teamcode.wheels;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.robot.Robot;

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

    public void setSpeed(double speed, WHEEL_DIAGONAL diagonal) {
        if (!isAvailable()) {
            return;
        }

        for (WheelMotor motor : diagonal.getWheels(config)) {
            motor.motor.setPower(speed * speedScale);
        }

    }

    public void translate(double xMagnitude, double yMagnitude, double rotation){

        // modified code from https://ftcforum.usfirst.org/forum/ftc-technology/android-studio/6361-mecanum-wheels-drive-code-example
        // from dmssargent

        double r = Math.hypot(xMagnitude, yMagnitude);
        double robotAngle = Math.atan2(yMagnitude, xMagnitude) - Math.PI / 4;
        final double v1 = (r * Math.cos(robotAngle)) + rotation;
        final double v2 = (r * Math.sin(robotAngle)) - rotation;
        final double v3 = (r * Math.sin(robotAngle)) + rotation;
        final double v4 = (r * Math.cos(robotAngle)) - rotation;

        // except for this, this is mine

        setSpeed(v1, MOTOR_SIDE.LEFT, MOTOR_END.FRONT);
        setSpeed(v2, MOTOR_SIDE.RIGHT, MOTOR_END.FRONT);
        setSpeed(v3, MOTOR_SIDE.LEFT, MOTOR_END.BACK);
        setSpeed(v4, MOTOR_SIDE.RIGHT, MOTOR_END.BACK);

    }

    @Override
    public void loop(Gamepad pad) {

        double lStickX = cleanJoystick(pad.left_stick_x);
        double lStickY = cleanJoystick(-pad.left_stick_y);
        double rStickX = pad.right_stick_x;

        translate(lStickX, lStickY, rStickX);

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
