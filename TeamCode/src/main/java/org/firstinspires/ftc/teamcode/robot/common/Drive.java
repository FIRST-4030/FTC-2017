package org.firstinspires.ftc.teamcode.robot.common;

import org.firstinspires.ftc.teamcode.driveto.DriveTo;
import org.firstinspires.ftc.teamcode.driveto.DriveToComp;
import org.firstinspires.ftc.teamcode.driveto.DriveToListener;
import org.firstinspires.ftc.teamcode.driveto.DriveToParams;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.utils.Heading;
import org.firstinspires.ftc.teamcode.wheels.MOTOR_SIDE;

public class Drive implements CommonTask, DriveToListener {

    /**
     * Configured drive constants
     */
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
    // Ratio of encoder ticks to millimeters driven
    public final static float ENCODER_PER_MM = .92f;
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
        GYROSCOPE,
        GYROSCOPE_SLAVE
    }

    public DriveTo distance(int distance) {
        robot.wheels.setTeleop(false);

        // Skip this motion if the error tolerance exceeds the target
        if (Math.abs(distance) <= OVERRUN_ENCODER) {
            return null;
        }

        // Calculate the drive in encoder ticks relative to the current position
        DriveToParams param = new DriveToParams(this, SENSOR_TYPE.DRIVE_ENCODER);
        int target = (int) ((float) distance * ENCODER_PER_MM) + robot.wheels.getEncoder();

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

        // Calculate the turn relative to our current heading
        double degrees = Heading.normalize(heading - robot.gyro.getHeading());

        // If the turn is more than 180 CW turn CCW instead
        if (degrees > Heading.HALF_CIRCLE) {
            degrees -= Heading.FULL_CIRCLE;
        }

        // Execute with this.degrees()
        return degrees(degrees);
    }

    public DriveTo degrees(double degrees) {
        robot.wheels.setTeleop(false);

        // Skip this motion if the error tolerance exceeds the target
        if (Math.abs(degrees) <= OVERRUN_GYRO) {
            return null;
        }

        // Calculate the drive in degrees relative to our current heading
        DriveToParams param = new DriveToParams(this, SENSOR_TYPE.GYROSCOPE);
        double heading = robot.gyro.getHeading();
        double target = Heading.normalize(heading + degrees);

        // Drive CW or CCW as selected
        if (degrees > 0) {
            param.rotationGreater(target - OVERRUN_GYRO);
        } else {
            param.rotationLess(target + OVERRUN_GYRO);
        }
        return new DriveTo(new DriveToParams[]{param});
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
            case GYROSCOPE_SLAVE:
                value = robot.gyro.getHeading();
                break;
        }
        return value;
    }

    @Override
    public void driveToRun(DriveToParams param) {
        switch ((SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
                if (param.comparator == COMP_FORWARD) {
                    robot.wheels.setSpeed(SPEED_FORWARD_SLOW);
                } else {
                    robot.wheels.setSpeed(SPEED_REVERSE);
                }
                break;
            case GYROSCOPE:
                if (param.comparator == COMP_CLOCKWISE) {
                    robot.wheels.setSpeed(SPEED_TURN, MOTOR_SIDE.LEFT);
                    robot.wheels.setSpeed(-SPEED_TURN, MOTOR_SIDE.RIGHT);
                } else {
                    robot.wheels.setSpeed(-SPEED_TURN, MOTOR_SIDE.LEFT);
                    robot.wheels.setSpeed(SPEED_TURN, MOTOR_SIDE.RIGHT);
                }
                break;
        }
    }
}
