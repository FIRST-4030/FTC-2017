package org.firstinspires.ftc.teamcode.sensors.gyro;

import org.firstinspires.ftc.teamcode.utils.Config;

public class GyroConfig implements Config {
    public final String name;
    public final GYRO_TYPES type;

    public GyroConfig(GYRO_TYPES type, String name) {
        this.name = name;
        this.type = type;
    }
}
