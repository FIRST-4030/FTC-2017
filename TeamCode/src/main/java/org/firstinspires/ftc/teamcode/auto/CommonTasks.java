package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.config.MotorConfigs;
import org.firstinspires.ftc.teamcode.config.ServoConfigs;
import org.firstinspires.ftc.teamcode.config.WheelMotorConfigs;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;
import org.firstinspires.ftc.teamcode.wheels.TankDrive;

import static org.firstinspires.ftc.teamcode.auto.DriveToMethods.LIFT_SPEED_UP;

public class CommonTasks {
    private static final double LIFT_DELAY = 0.75;
    private static final double CLAW_DELAY = 0.25;
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

    private HardwareMap map = null;
    private Telemetry telemetry = null;
    private LIFT_STATE liftState = LIFT_STATE.INIT;

    public CommonTasks(HardwareMap map, Telemetry telemetry) {
        this.map = map;
        this.telemetry = telemetry;
    }

    public void initDrive(TankDrive tank) {
        tank = new WheelMotorConfigs().init(map, telemetry);
        tank.stop();
    }

    public void initLift(Motor lift) {
        lift = new MotorConfigs().init(map, telemetry, "LIFT");
        lift.stop();
    }

    public void initClaws(ServoFTC top, ServoFTC bottom) {
        top = new ServoConfigs().init(map, telemetry, "CLAW-TOP");
        top.min();
        bottom = new ServoConfigs().init(map, telemetry, "CLAW-BOTTOM");
        bottom.min();
    }

    public void liftZero(Motor lift) {

    }

    public AutoTracker liftAutoStart(Motor lift, ServoFTC top, ServoFTC bottom) {
        AutoTracker auto = new AutoTracker();

        switch (liftState) {
            case INIT:
                liftState = liftState.next();
                break;
            case GRAB:
                top.max();
                bottom.min();
                auto.interval = CLAW_DELAY;
                liftState = liftState.next();
                break;
            case LIFT:
                lift.setPower(LIFT_SPEED_UP);
                auto.interval = LIFT_DELAY;
                liftState = liftState.next();
                break;
            case READY:
                lift.stop();
                liftState = liftState.next();
                break;
            case DONE:
                auto.done = true;
                break;
        }

        return auto;
    }
}
