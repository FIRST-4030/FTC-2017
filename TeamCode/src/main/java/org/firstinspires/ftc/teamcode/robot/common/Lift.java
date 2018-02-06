package org.firstinspires.ftc.teamcode.robot.common;

import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.robot.INTAKES;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;

public class Lift implements CommonTask {

    // Intake speed
    public final static float INTAKE_SPEED_IN = 1.0f;
    public final static float INTAKE_SPEED_OUT = -0.5f * INTAKE_SPEED_IN;

    // Lift speed -- Up is motor positive, ticks increasing
    public final static float LIFT_SPEED_UP = 1.0f;
    public final static float LIFT_SPEED_DOWN = -LIFT_SPEED_UP;

    // Eject constants
    private final static float EJECT_DELAY = 0.5f;
    private final static int WIGGLE_MILLS = 75;
    private final static int REVERSE_MM = 125;

    // Runtime
    private final Robot robot;
    private LIFT_STATE liftState = LIFT_STATE.INIT;
    private EJECT_STATE ejectState = EJECT_STATE.INIT;

    public Lift(Robot robot) {
        this.robot = robot;
    }

    public AutoDriver autoStart(AutoDriver driver) {
        switch (liftState) {
            case INIT:
                driver.done = false;
                liftState = liftState.next();
                break;
            case START:
                robot.jewelArm.setPosition(Common.JEWEL_ARM_RETRACT);
                for (INTAKES intake : INTAKES.values()) {
                    robot.intakes[intake.ordinal()].setPower(INTAKE_SPEED_IN);
                }
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
        START,
        DONE;

        public LIFT_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public LIFT_STATE next() {
            return OrderedEnumHelper.next(this);
        }
    }

    public AutoDriver eject(AutoDriver driver) {
        switch (ejectState) {
            case INIT:
                driver.done = false;
                ejectState = ejectState.next();
                break;
            case EJECT:
                for (INTAKES intake : INTAKES.values()) {
                    robot.intakes[intake.ordinal()].setPower(INTAKE_SPEED_OUT);
                }
                driver.interval = Lift.EJECT_DELAY;
                ejectState = ejectState.next();
                break;
            case WIGGLE:
                driver.drive = robot.common.drive.timeTurn(WIGGLE_MILLS, Drive.SPEED_FORWARD_SLOW);
                ejectState = ejectState.next();
                break;
            case REVERSE:
                driver.drive = robot.common.drive.distance(-REVERSE_MM);
                ejectState = ejectState.next();
                break;
            case STOP:
                for (INTAKES intake : INTAKES.values()) {
                    robot.intakes[intake.ordinal()].stop();
                }
                ejectState = ejectState.next();
                break;
            case DONE:
                driver.done = true;
                break;
        }
        return driver;
    }

    enum EJECT_STATE implements OrderedEnum {
        INIT,
        EJECT,
        WIGGLE,
        REVERSE,
        STOP,
        DONE;

        public EJECT_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public EJECT_STATE next() {
            return OrderedEnumHelper.next(this);
        }
    }
}
