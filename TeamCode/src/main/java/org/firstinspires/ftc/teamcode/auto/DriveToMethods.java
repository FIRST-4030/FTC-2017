package org.firstinspires.ftc.teamcode.auto;

import org.firstinspires.ftc.teamcode.driveto.DriveTo;
import org.firstinspires.ftc.teamcode.driveto.DriveToComp;
import org.firstinspires.ftc.teamcode.driveto.DriveToListener;
import org.firstinspires.ftc.teamcode.driveto.DriveToParams;
import org.firstinspires.ftc.teamcode.sensors.Gyro;
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

    // Forward is toward the claws, motor positive, ticks increasing
    public final static DriveToComp COMP_FORWARD = DriveToComp.GREATER;
    public final static float SPEED_FORWARD = 1.0f;
    public final static float SPEED_FORWARD_SLOW = SPEED_FORWARD * 0.75f;
    public final static float SPEED_REVERSE = -SPEED_FORWARD;

    // Up is motor positive, ticks <unknown>
    public final static float LIFT_SPEED_UP = 1.0f;
    public final static float LIFT_SPEED_DOWN = -LIFT_SPEED_UP;

    // Sensor reference types for our DriveTo callbacks
    public enum SENSOR_TYPE {
        DRIVE_ENCODER,
        GYROSCOPE
    }

    public static DriveTo driveForward(DriveToListener listener, TankDrive tank, int distance) {
        tank.setTeleop(false);
        DriveToParams param = new DriveToParams(listener, SENSOR_TYPE.DRIVE_ENCODER);
        int ticks = (int) ((float) distance * ENCODER_PER_MM);
        param.greaterThan(ticks + tank.getEncoder() - OVERRUN_ENCODER);
        return new DriveTo(new DriveToParams[]{param});
    }

    public static DriveTo driveBackward(DriveToListener listener, TankDrive tank, int distance) {
        tank.setTeleop(false);
        DriveToParams param = new DriveToParams(listener, SENSOR_TYPE.DRIVE_ENCODER);
        int ticks = (int) ((float) -distance * ENCODER_PER_MM);
        param.lessThan(ticks - tank.getEncoder() - OVERRUN_ENCODER);
        return new DriveTo(new DriveToParams[]{param});
    }

//    /**
//     *
//     * @param listener
//     * @param tank
//     * @param degrees Positive degerees indicate turning clockwise
//     * @return
//     */
//    public static DriveTo turnDegrees(DriveToListener listener, TankDrive tank, double degrees) {
//
//        tank.setTeleop(false);
//
//
//    }

    public static void stop(TankDrive tank, DriveToParams param) {
        switch ((SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
                tank.stop();
                break;
        }
    }

    public static double sensor(TankDrive tank, DriveToParams param) {
        double value = 0;
        switch ((SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
                value = tank.getEncoder();
                break;
        }
        return value;
    }

    public static void run(TankDrive tank, DriveToParams param) {
        switch ((SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
                if (param.comparator == COMP_FORWARD) {
                    tank.setSpeed(SPEED_FORWARD_SLOW);
                } else {
                    tank.setSpeed(SPEED_REVERSE);
                }
                break;
        }
    }
}
