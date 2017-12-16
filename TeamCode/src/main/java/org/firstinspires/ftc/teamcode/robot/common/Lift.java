package org.firstinspires.ftc.teamcode.robot.common;

import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.robot.CLAWS;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;

public class Lift implements CommonTask {

    // LiftAutoStart constants
    public static final double LIFT_DELAY = 0.5;
    private static final double CLAW_DELAY = 0.25;

    // Lift speed -- Up is motor positive, ticks increasing
    public final static float LIFT_SPEED_UP = 1.0f;
    public final static float LIFT_SPEED_DOWN = -LIFT_SPEED_UP;

    // Runtime
    private final Robot robot;
    private LIFT_STATE liftState = LIFT_STATE.INIT;

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

    public Lift(Robot robot) {
        this.robot = robot;
    }

    public AutoDriver autoStart() {
        AutoDriver driver = new AutoDriver();

        switch (liftState) {
            case INIT:
                robot.jewelArm.setPosition(Common.JEWEL_ARM_RETRACT);
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
}
