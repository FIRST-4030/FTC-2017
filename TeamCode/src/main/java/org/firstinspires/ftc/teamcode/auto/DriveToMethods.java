package org.firstinspires.ftc.teamcode.auto;

import org.firstinspires.ftc.teamcode.driveto.DriveTo;
import org.firstinspires.ftc.teamcode.driveto.DriveToListener;
import org.firstinspires.ftc.teamcode.driveto.DriveToParams;
import org.firstinspires.ftc.teamcode.wheels.TankDrive;

/*
 * These are robot-specific helper methods
 * They exist to encourage code re-use across classes using DriveTo methods
 *
 * They are a reasonable template for future robots, but are unlikely to work as-is
 */
public class DriveToMethods {

    // Ratio of encoder ticks to millimeters driven
    public final static float ENCODER_PER_MM = 1.15f;
    // An estimate of the number of ticks we continue on inertia after calling tank.stop()
    public final static int OVERRUN_ENCODER = 10;

    // Forward is toward the claws
    public final static float SPEED_FORWARD = 1.0f;
    public final static float SPEED_REVERSE = -SPEED_FORWARD;

    public final static float LIFT_SPEED_UP = 1.0f;
    public final static float LIFT_SPEED_DOWN = -LIFT_SPEED_UP;

    // Sensor reference types for our DriveTo callbacks
    public enum SENSOR_TYPE {
        DRIVE_ENCODER
    }

    // On the 2017 robot, forward (toward claws) is ticks increasing
    public static DriveTo driveForward(DriveToListener listener, TankDrive tank, int distance) {
        tank.setTeleop(false);
        DriveToParams param = new DriveToParams(listener, SENSOR_TYPE.DRIVE_ENCODER);
        int ticks = (int) ((float) distance * ENCODER_PER_MM);
        param.greaterThan(ticks + tank.getEncoder() - OVERRUN_ENCODER);
        return new DriveTo(new DriveToParams[]{param});
    }
}
