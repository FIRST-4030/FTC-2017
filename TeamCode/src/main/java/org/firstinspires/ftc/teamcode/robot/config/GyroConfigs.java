package org.firstinspires.ftc.teamcode.robot.config;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.sensors.gyro.GYRO_TYPES;
import org.firstinspires.ftc.teamcode.sensors.gyro.Gyro;
import org.firstinspires.ftc.teamcode.sensors.gyro.GyroConfig;
import org.firstinspires.ftc.teamcode.sensors.gyro.MRGyro;
import org.firstinspires.ftc.teamcode.sensors.gyro.RevIMU;
import org.firstinspires.ftc.teamcode.config.BOT;
import org.firstinspires.ftc.teamcode.config.Configs;

public class GyroConfigs extends Configs {
    public GyroConfigs(HardwareMap map, Telemetry telemetry, BOT bot) {
        super(map, telemetry, bot);
    }

    public Gyro init() {
        GyroConfig config = config();
        super.checkConfig(config);
        Gyro gyro = null;
        switch (config.type) {
            case REV:
                gyro = new RevIMU(map, telemetry, config.name);
                break;
            case MR:
                gyro = new MRGyro(map, telemetry, config.name);
                break;
        }

        // Don't check for the REV gyro -- it has delayed init and will log() its own error
        if (config.type != GYRO_TYPES.REV) {
            super.checkAvailable(gyro);
        }
        return gyro;
    }

    public GyroConfig config() {
        super.checkBOT();

        GyroConfig config = null;
        switch (bot) {
            case WestCoast:
                config = new GyroConfig(GYRO_TYPES.REV, "imu");
                break;
            case Mecanum:
                config = new GyroConfig(GYRO_TYPES.MR, "gyro");
                break;
            case WestCoastClaw:
                config = new GyroConfig(GYRO_TYPES.REV, "imu");
                break;
        }
        return config;
    }
}
