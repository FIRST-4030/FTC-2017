package org.firstinspires.ftc.teamcode.robot.auto;

import org.firstinspires.ftc.teamcode.driveto.DriveTo;
import org.firstinspires.ftc.teamcode.driveto.DriveToComp;
import org.firstinspires.ftc.teamcode.driveto.DriveToListener;
import org.firstinspires.ftc.teamcode.driveto.DriveToParams;
import org.firstinspires.ftc.teamcode.robot.CLAWS;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.utils.Heading;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;
import org.firstinspires.ftc.teamcode.wheels.MOTOR_SIDE;

/*
 * These are robot-specific helper methods
 * They exist to encourage code re-use across classes
 *
 * They are a reasonable template for future robots, but are unlikely to work as-is
 */
public class CommonTasks implements DriveToListener {

    // LiftAutoStart constants
    private static final double LIFT_DELAY = 0.75;
    private static final double CLAW_DELAY = 0.25;

    // Jewel arm post-start retracted position
    public static final double JEWEL_ARM_RETRACT = 0.25d;

    /**
     * Configured drive constants
     */
    // Straight drive speed -- Forward is toward the claws, motor positive, tick increasing
    public final static float SPEED_FORWARD = 1.0f;
    public final static float SPEED_FORWARD_SLOW = SPEED_FORWARD * 0.75f;
    public final static float SPEED_REVERSE = -SPEED_FORWARD;
    // Turn drive speed
    public final static float SPEED_TURN = SPEED_FORWARD * 0.375f;
    // Lift speed -- Up is motor positive, ticks increasing
    public final static float LIFT_SPEED_UP = 1.0f;
    public final static float LIFT_SPEED_DOWN = -LIFT_SPEED_UP;

    /**
     * Tuned drive constants
     */
    // An estimate of the number of degrees we slip on inertia after calling wheels.stop()
    public final static int OVERRUN_GYRO = 0; // TBD
    // An estimate of the number of ricks we slip on inertia after calling wheels.stop()
    public final static int OVERRUN_ENCODER = 10;

    /**
     * Physical-logical mapping
     */
    // Ratio of encoder ticks to millimeters driven
    public final static float ENCODER_PER_MM = 1.15f;
    // Clockwise is gryo-increasing
    public final static DriveToComp COMP_CLOCKWISE = DriveToComp.GREATER;
    // Forward is toward the claws, motor positive, ticks increasing
    public final static DriveToComp COMP_FORWARD = DriveToComp.GREATER;

    // Runtime
    private final Robot robot;
    private LIFT_STATE liftState = LIFT_STATE.INIT;

    public CommonTasks(Robot robot) {
        this.robot = robot;
    }

    public AutoDriver liftAutoStart() {
        AutoDriver driver = new AutoDriver();

        switch (liftState) {
            case INIT:
                liftState = liftState.next();
                break;
            case GRAB:
                robot.claws[CLAWS.TOP.ordinal()].max();
                robot.claws[CLAWS.BOTTOM.ordinal()].min();
                driver.interval = CLAW_DELAY;
                liftState = liftState.next();
                break;
            case LIFT:
                robot.lift.setPower(LIFT_SPEED_UP);
                driver.interval = LIFT_DELAY;
                liftState = liftState.next();
                break;
            case READY:
                robot.lift.stop();
                liftState = liftState.next();
                break;
            case DONE:
                driver.done = true;
                break;
        }

        return driver;
    }

    enum LIFT_STATE implements OrderedEnum {
        INIT,
        GRAB,
        LIFT,
        READY,
        DONE;

        public LIFT_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public LIFT_STATE next() {
            return OrderedEnumHelper.next(this);
        }
    }

    // Sensor reference types for our DriveTo callbacks
    public enum SENSOR_TYPE {
        DRIVE_ENCODER,
        GYROSCOPE,
        GYROSCOPE_SLAVE
    }

    public DriveTo driveForward(int distance) {
        robot.wheels.setTeleop(false);
        distance = Math.abs(distance);
        DriveToParams param = new DriveToParams(this, SENSOR_TYPE.DRIVE_ENCODER);
        int ticks = (int) ((float) distance * ENCODER_PER_MM);
        param.greaterThan(ticks + robot.wheels.getEncoder() - OVERRUN_ENCODER);
        return new DriveTo(new DriveToParams[]{param});
    }

    public DriveTo driveBackward(int distance) {
        robot.wheels.setTeleop(false);
        distance = -Math.abs(distance);
        DriveToParams param = new DriveToParams(this, SENSOR_TYPE.DRIVE_ENCODER);
        int ticks = (int) ((float) distance * ENCODER_PER_MM);
        param.lessThan(ticks + robot.wheels.getEncoder() - OVERRUN_ENCODER);
        return new DriveTo(new DriveToParams[]{param});
    }

    public DriveTo turnDegrees(int degrees) {
        DriveToParams[] params = new DriveToParams[2];
        params[0] = new DriveToParams(this, SENSOR_TYPE.GYROSCOPE_SLAVE);
        params[1] = new DriveToParams(this, SENSOR_TYPE.GYROSCOPE);

        // Current and target heading in normalized degrees
        int heading = robot.gyro.getHeading();
        int target = Heading.normalize(heading + degrees);
        int opposite = Heading.normalize(target + (Heading.FULL_CIRCLE / 2));

        // Match both the target and its opposite to ensure we can turn through 0 degrees
        // Set the sensor type to GYRO_SLAVE for the opposite so we don't drive with it
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
