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
    enum CLAWS {TOP, BOTTOM;}

    private static final double LIFT_DELAY = 0.75;
    private static final double CLAW_DELAY = 0.25;

    private HardwareMap map = null;
    private Telemetry telemetry = null;
    private LIFT_STATE liftState = LIFT_STATE.INIT;

    public CommonTasks(HardwareMap map, Telemetry telemetry) {
        this.map = map;
        this.telemetry = telemetry;
    }

    public TankDrive initDrive() {
        TankDrive tank = new WheelMotorConfigs().init(map, telemetry);
        tank.stop();
        return tank;
    }

    public Motor initLift() {
        Motor lift = new MotorConfigs().init(map, telemetry, "LIFT");
        lift.stop();
        return lift;
    }

    public ServoFTC[] initClaws() {
        ServoFTC[] claws = new ServoFTC[CLAWS.values().length];
        claws[CLAWS.TOP.ordinal()] = new ServoConfigs().init(map, telemetry, "CLAW-TOP");
        claws[CLAWS.BOTTOM.ordinal()] = new ServoConfigs().init(map, telemetry, "CLAW-BOTTOM");
        for (ServoFTC claw : claws) {
            claw.min();
        }
        return claws;
    }

    public AutoDriver liftAutoStart(Motor lift, ServoFTC[] claws) {
        AutoDriver driver = new AutoDriver();

        switch (liftState) {
            case INIT:
                liftState = liftState.next();
                break;
            case GRAB:
                claws[CLAWS.TOP.ordinal()].max();
                claws[CLAWS.BOTTOM.ordinal()].min();
                driver.interval = CLAW_DELAY;
                liftState = liftState.next();
                break;
            case LIFT:
                lift.setPower(LIFT_SPEED_UP);
                driver.interval = LIFT_DELAY;
                liftState = liftState.next();
                break;
            case READY:
                lift.stop();
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
}
