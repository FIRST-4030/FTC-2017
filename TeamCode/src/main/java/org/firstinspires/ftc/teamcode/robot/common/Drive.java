package org.firstinspires.ftc.teamcode.robot.common;

import org.firstinspires.ftc.teamcode.driveto.DriveTo;
import org.firstinspires.ftc.teamcode.driveto.DriveToComp;
import org.firstinspires.ftc.teamcode.driveto.DriveToListener;
import org.firstinspires.ftc.teamcode.driveto.DriveToParams;
import org.firstinspires.ftc.teamcode.driveto.PIDParams;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.utils.Heading;
import org.firstinspires.ftc.teamcode.wheels.MOTOR_SIDE;

public class Drive implements CommonTask, DriveToListener {

    /**
     * Configured drive constants
     */
    // PID Turns
    public static final double TURN_TOLERANCE = 1.0d / (double) Heading.FULL_CIRCLE;
    public static final PIDParams TURN_PARAMS = new PIDParams(0.1d, 0.01d, 0.0d);
    // Straight drive speed -- Forward is toward the claws, motor positive, tick increasing
    public final static float SPEED_FORWARD = 1.0f;
    public final static float SPEED_FORWARD_SLOW = SPEED_FORWARD * 0.75f;
    public final static float SPEED_REVERSE = -SPEED_FORWARD;
    // Turn drive speed
    public final static float SPEED_TURN = SPEED_FORWARD * 0.5f;

    /**
     * Tuned drive constants
     */
    // An estimate of the number of degrees we slip on inertia after calling wheels.stop()
    public final static int OVERRUN_GYRO = 1;
    // An estimate of the number of ricks we slip on inertia after calling wheels.stop()
    public final static int OVERRUN_ENCODER = 10;

    /**
     * Physical-logical mapping
     */
    // Clockwise is gryo-increasing
    public final static DriveToComp COMP_CLOCKWISE = DriveToComp.ROTATION_GREATER;
    // Forward is toward the claws, motor positive, ticks increasing
    public final static DriveToComp COMP_FORWARD = DriveToComp.GREATER;


    // Runtime
    private final Robot robot;

    public Drive(Robot robot) {
        this.robot = robot;
    }

    // Sensor reference types for our DriveTo callbacks
    public enum SENSOR_TYPE {
        DRIVE_ENCODER,
        GYROSCOPE
    }

    public DriveTo distance(int distance) {
        robot.wheels.setTeleop(false);

        // Skip this motion if the error tolerance exceeds the target
        if (Math.abs(distance) <= OVERRUN_ENCODER) {
            return null;
        }

        // Calculate the drive in encoder ticks relative to the current position
        DriveToParams param = new DriveToParams(this, SENSOR_TYPE.DRIVE_ENCODER);
        int target = (int) ((double) distance * robot.wheels.getTicksPerMM()) + robot.wheels.getEncoder();

        // Drive forward or backward as selected
        if (distance > 0) {
            param.greaterThan(target - OVERRUN_ENCODER);
        } else {
            param.lessThan(target - OVERRUN_ENCODER);
        }
        return new DriveTo(new DriveToParams[]{param});
    }

    public DriveTo heading(double heading) {
        robot.wheels.setTeleop(false);
        DriveToParams param = new DriveToParams(this, SENSOR_TYPE.GYROSCOPE);
        param.rotationPid(heading, TURN_TOLERANCE, TURN_PARAMS);
        return new DriveTo(new DriveToParams[]{param});
    }

    public DriveTo degrees(double degrees) {
        double heading = Heading.normalize(degrees + robot.gyro.getHeading());
        return heading(heading);
    }

    @Override
    public void driveToStop(DriveToParams param) {
        switch ((SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
            case GYROSCOPE:
                robot.wheels.stop();
                break;
        }
    }

    @Override
    public double driveToSensor(DriveToParams param) {
        double value = 0;
        switch ((SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
                value = robot.wheels.getEncoder();
                break;
            case GYROSCOPE:
                value = robot.gyro.getHeading();
                break;
        }
        return value;
    }

    @Override
    public void driveToRun(DriveToParams param) {
        double speed;
        switch ((SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
                if (param.comparator == COMP_FORWARD) {
                    robot.wheels.setSpeed(SPEED_FORWARD_SLOW);
                } else {
                    robot.wheels.setSpeed(SPEED_REVERSE);
                }
                break;
            case GYROSCOPE:
                switch (param.comparator) {
                    case ROTATION_PID:
                        speed = param.pid.output();
                        robot.wheels.setSpeed(speed, MOTOR_SIDE.LEFT);
                        robot.wheels.setSpeed(-speed, MOTOR_SIDE.RIGHT);
                        break;
                    default:
                        throw new IllegalStateException("Unhandled driveToRun: " +
                                param.reference + "::" + param.comparator);
                }
                break;
            default:
                throw new IllegalStateException("Unhandled driveToRun: " +
                        param.reference + " ::" + param.comparator);
        }
    }
}
