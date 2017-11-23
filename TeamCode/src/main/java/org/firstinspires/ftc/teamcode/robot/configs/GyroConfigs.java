package org.firstinspires.ftc.teamcode.robot.configs;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.sensors.gyro.GYRO_TYPES;
import org.firstinspires.ftc.teamcode.sensors.gyro.Gyro;
import org.firstinspires.ftc.teamcode.sensors.gyro.GyroConfig;
import org.firstinspires.ftc.teamcode.sensors.gyro.MRGyro;
import org.firstinspires.ftc.teamcode.sensors.gyro.RevIMU;

public class GyroConfigs extends Configs {
    public GyroConfigs(HardwareMap map, Telemetry telemetry, BOT bot) {
        super(map, telemetry, bot);
    }

    public Gyro init() {
        GyroConfig config = config(bot);
        if (config == null) {
            throw new IllegalArgumentException(this.getClass().getName() + ": Not configured: " + bot);
        }
        Gyro gyro = null;
        switch (config.type) {
            case REV:
                gyro = new RevIMU(map, telemetry, config.name);
                break;
            case MR:
                gyro = new MRGyro(map, telemetry, config.name);
                break;
        }
        if (gyro == null || !gyro.isAvailable()) {
            telemetry.log().add(this.getClass().getName() + ": Unable to initialize: " + bot);
        }
        return gyro;
    }

    public static GyroConfig config(BOT bot) {
        GyroConfig config = null;
        if (bot == null) {
            throw new IllegalArgumentException("Null BOT");
        }
        switch (bot) {
            case WestCoast:
                config = new GyroConfig(GYRO_TYPES.REV, "imu");
                break;
            case Mecanum:
                config = new GyroConfig(GYRO_TYPES.MR, "gyro");
                break;
        }
        return config;
    }
}
