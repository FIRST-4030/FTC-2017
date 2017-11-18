package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.config.BOT;
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

    private WheelMotorConfigs wheels = null;
    private ServoConfigs servos = null;
    private MotorConfigs motors = null;
    private HardwareMap map = null;
    private Telemetry telemetry = null;
    private LIFT_STATE liftState = LIFT_STATE.INIT;

    public CommonTasks(HardwareMap map, Telemetry telemetry, BOT bot) {
        this.map = map;
        this.telemetry = telemetry;
        wheels = new WheelMotorConfigs(map, telemetry, bot);
        servos = new ServoConfigs(map, telemetry, bot);
        motors = new MotorConfigs(map, telemetry, bot);
    }

    public CommonTasks(HardwareMap map, Telemetry telemetry) {
        this(map, telemetry, null);
    }

    public TankDrive initDrive() {
        TankDrive tank = wheels.init();
        tank.stop();
        return tank;
    }

    public Motor initLift() {
        Motor lift = motors.init("LIFT");
        lift.stop();
        return lift;
    }

    public ServoFTC[] initClaws() {
        ServoFTC[] claws = new ServoFTC[CLAWS.values().length];
        claws[CLAWS.TOP.ordinal()] = servos.init("CLAW-TOP");
        claws[CLAWS.BOTTOM.ordinal()] = servos.init("CLAW-BOTTOM");
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
