package org.firstinspires.ftc.teamcode.robot.configs;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.wheels.MOTOR_SIDE;
import org.firstinspires.ftc.teamcode.wheels.TankConfig;
import org.firstinspires.ftc.teamcode.wheels.TankDrive;
import org.firstinspires.ftc.teamcode.wheels.TankMotor;

public class WheelConfigs extends Configs {
    public WheelConfigs(HardwareMap map, Telemetry telemetry, BOT bot) {
        super(map, telemetry, bot);
    }

    public TankDrive init() {
        TankConfig config = config(bot);
        if (config == null) {
            throw new IllegalArgumentException(this.getClass().getName() + ": Not configured: " + bot);
        }
        TankDrive tank = new TankDrive(map, telemetry, config);
        if (!tank.isAvailable()) {
            telemetry.log().add(this.getClass().getName() + ": Unable to initialize: " + bot);
        }
        return tank;
    }

    public static TankConfig config(BOT bot) {
        TankMotor[] motors;
        TankConfig config = null;
        if (bot == null) {
            throw new IllegalArgumentException("Null BOT");
        }
        switch (bot) {
            case WestCoast:
                motors = new TankMotor[4];
                motors[0] = new TankMotor("ML1", MOTOR_SIDE.LEFT, true);
                motors[1] = new TankMotor("MR1", MOTOR_SIDE.RIGHT);
                motors[2] = new TankMotor("ML2", MOTOR_SIDE.LEFT, true);
                motors[3] = new TankMotor("MR2", MOTOR_SIDE.RIGHT);
                config = new TankConfig(motors, 0, 1.0d);
                break;
            case Mecanum:
                motors = new TankMotor[2];
                motors[0] = new TankMotor("L", MOTOR_SIDE.LEFT);
                motors[1] = new TankMotor("R", MOTOR_SIDE.RIGHT, true);
                config = new TankConfig(motors, 0, 1.0d);
                break;
        }
        return config;
    }
}
