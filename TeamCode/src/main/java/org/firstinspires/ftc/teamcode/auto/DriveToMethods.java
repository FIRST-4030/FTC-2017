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

    // An Estimate of the number of ticks we continue on inertia after stopping
    public final static int OVERRUN_GYRO = 10; // TBD
    // Degrees in a circle, for use in MOD math
    public final static int FULL_CIRCLE = 360;

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
        distance = Math.abs(distance);
        DriveToParams param = new DriveToParams(listener, SENSOR_TYPE.DRIVE_ENCODER);
        int ticks = (int) ((float) distance * ENCODER_PER_MM);
        param.greaterThan(ticks + tank.getEncoder() - OVERRUN_ENCODER);
        return new DriveTo(new DriveToParams[]{param});
    }

    public static DriveTo driveBackward(DriveToListener listener, TankDrive tank, int distance) {
        tank.setTeleop(false);
        distance = -Math.abs(distance);
        DriveToParams param = new DriveToParams(listener, SENSOR_TYPE.DRIVE_ENCODER);
        int ticks = (int) ((float) distance * ENCODER_PER_MM);
        param.lessThan(-1 * (ticks + tank.getEncoder() - OVERRUN_ENCODER));
        return new DriveTo(new DriveToParams[]{param});
    }

    /**
     * This method presents a very serious problem - if you try to go to a heading that isn't
     * in the range (-180, 180), it will rotate forever, as the desired heading isn't reachable.
     *
     * @param listener
     * @param gyro
     * @param degrees  Positive degerees indicate turning clockwise
     * @return
     * @author Bryan Cook
     */
    public static DriveTo turnDegrees(DriveToListener listener, Gyro gyro, int degrees) {
        DriveToParams[] params = new DriveToParams[2];
        params[0] = new DriveToParams(listener, SENSOR_TYPE.GYROSCOPE);
        params[1] = new DriveToParams(listener, SENSOR_TYPE.GYROSCOPE);

        // Current and target heading in normalized degrees
        int heading = gyro.getHeading();
        int target = Gyro.normalizeHeading(heading + degrees);
        int opposite = Gyro.normalizeHeading(target + (FULL_CIRCLE / 2));

        // Match both the target and its opposite to ensure we can turn through 0 degrees
        if (degrees > 0) {
            params[0].lessThan(opposite);
            params[1].greaterThan(target - OVERRUN_GYRO);
        } else {
            params[0].greaterThan(opposite);
            params[1].lessThan(target + OVERRUN_GYRO);
        }

        // Default match mode is "all", so both parameters must match as the same time
        return new DriveTo(params);
    }

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
