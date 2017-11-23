package org.firstinspires.ftc.teamcode.sensors.gyro;

public class GyroConfig {
    public final String name;
    public final GYRO_TYPES type;

    public GyroConfig(GYRO_TYPES type, String name) {
        this.name = name;
        this.type = type;
    }
}
