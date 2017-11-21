package org.firstinspires.ftc.teamcode.auto;

import org.firstinspires.ftc.teamcode.driveto.DriveTo;
import org.firstinspires.ftc.teamcode.driveto.DriveToComp;
import org.firstinspires.ftc.teamcode.driveto.DriveToListener;
import org.firstinspires.ftc.teamcode.driveto.DriveToParams;
import org.firstinspires.ftc.teamcode.robot.CLAWS;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.sensors.Gyro;
import org.firstinspires.ftc.teamcode.utils.AutoDriver;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;
import org.firstinspires.ftc.teamcode.wheels.MotorSide;

/*
 * These are robot-specific helper methods
 * They exist to encourage code re-use across classes
 *
 * They are a reasonable template for future robots, but are unlikely to work as-is
 */
public class CommonTasks {

    // LiftAutoStart constants
    private static final double LIFT_DELAY = 0.75;
    private static final double CLAW_DELAY = 0.25;

    /**
     * Configured drive constants
     */
    // Straight drive speed -- Forward is toward the claws, motor positive, tick increasing
    public final static float SPEED_FORWARD = 1.0f;
    public final static float SPEED_FORWARD_SLOW = SPEED_FORWARD * 0.75f;
    public final static float SPEED_REVERSE = -SPEED_FORWARD;
    // Turn drive speed
    public final static float SPEED_TURN = SPEED_FORWARD * 0.5f;
    // Lift speed -- Up is motor positive, ticks increasing
    public final static float LIFT_SPEED_UP = 1.0f;
    public final static float LIFT_SPEED_DOWN = -LIFT_SPEED_UP;

    /**
     * Tuned drive constants
     */
    // An estimate of the number of degrees we slip on inertia after calling wheels.stop()
    public final static int OVERRUN_GYRO = 1; // TBD
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

    // Geometric constants
    public final static int FULL_CIRCLE = 360;

    // Runtime
    private Robot robot;
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

    public static DriveTo driveForward(DriveToListener listener, Robot robot, int distance) {
        robot.tank.setTeleop(false);
        distance = Math.abs(distance);
        DriveToParams param = new DriveToParams(listener, SENSOR_TYPE.DRIVE_ENCODER);
        int ticks = (int) ((float) distance * ENCODER_PER_MM);
        param.greaterThan(ticks + robot.tank.getEncoder() - OVERRUN_ENCODER);
        return new DriveTo(new DriveToParams[]{param});
    }

    public static DriveTo driveBackward(DriveToListener listener, Robot robot, int distance) {
        robot.tank.setTeleop(false);
        distance = -Math.abs(distance);
        DriveToParams param = new DriveToParams(listener, SENSOR_TYPE.DRIVE_ENCODER);
        int ticks = (int) ((float) distance * ENCODER_PER_MM);
        param.lessThan(-1 * (ticks + robot.tank.getEncoder() - OVERRUN_ENCODER));
        return new DriveTo(new DriveToParams[]{param});
    }

    public static DriveTo turnDegrees(DriveToListener listener, Robot robot, int degrees) {
        DriveToParams[] params = new DriveToParams[2];
        params[0] = new DriveToParams(listener, SENSOR_TYPE.GYROSCOPE_SLAVE);
        params[1] = new DriveToParams(listener, SENSOR_TYPE.GYROSCOPE);

        // Current and target heading in normalized degrees
        int heading = robot.gyro.getHeading();
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

    public static void stop(Robot robot, DriveToParams param) {
        switch ((SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
                robot.tank.stop();
                break;
        }
    }

    public static double sensor(Robot robot, DriveToParams param) {
        double value = 0;
        switch ((SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
                value = robot.tank.getEncoder();
                break;
            case GYROSCOPE:
            case GYROSCOPE_SLAVE:
                value = robot.gyro.getHeading();
                break;
        }
        return value;
    }

    public static void run(Robot robot, DriveToParams param) {
        switch ((SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
                if (param.comparator == COMP_FORWARD) {
                    robot.tank.setSpeed(SPEED_FORWARD_SLOW);
                } else {
                    robot.tank.setSpeed(SPEED_REVERSE);
                }
                break;
            case GYROSCOPE:
                if (param.comparator == COMP_CLOCKWISE) {
                    robot.tank.setSpeed(SPEED_TURN, MotorSide.LEFT);
                    robot.tank.setSpeed(-SPEED_TURN, MotorSide.RIGHT);
                } else {
                    robot.tank.setSpeed(-SPEED_TURN, MotorSide.LEFT);
                    robot.tank.setSpeed(SPEED_TURN, MotorSide.RIGHT);
                }
                break;
        }
    }
}
