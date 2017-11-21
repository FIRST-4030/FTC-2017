package org.firstinspires.ftc.teamcode.robot.configs;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.wheels.MotorSide;
import org.firstinspires.ftc.teamcode.wheels.TankConfig;
import org.firstinspires.ftc.teamcode.wheels.TankDrive;
import org.firstinspires.ftc.teamcode.wheels.TankMotor;

public class WheelConfigs {
    private HardwareMap map = null;
    private Telemetry telemetry = null;
    private BOT bot = null;

    public WheelConfigs(HardwareMap map, Telemetry telemetry, BOT bot) {
        if (map == null || bot == null) {
            throw new IllegalArgumentException("Null HardwareMap or BOT");
        }
        this.map = map;
        this.telemetry = telemetry;
        this.bot = bot;
    }

    public TankDrive init() {
        TankConfig config = config(bot);
        if (config == null) {
            throw new IllegalArgumentException("No motor configured for: " + bot);
        }
        TankDrive tank = new TankDrive(map, telemetry, config);
        if (!tank.isAvailable()) {
            telemetry.log().add("Unable to initialize tank for: " + bot);
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
                motors[0] = new TankMotor("ML1", MotorSide.LEFT, true);
                motors[1] = new TankMotor("MR1", MotorSide.RIGHT);
                motors[2] = new TankMotor("ML2", MotorSide.LEFT, true);
                motors[3] = new TankMotor("MR2", MotorSide.RIGHT);
                config = new TankConfig(motors, 0, 1.0d);
                break;
            case Mecanum:
                motors = new TankMotor[2];
                motors[0] = new TankMotor("L", MotorSide.LEFT);
                motors[1] = new TankMotor("R", MotorSide.RIGHT, true);
                config = new TankConfig(motors, 0, 1.0d);
                break;
        }
        return config;
    }
}
