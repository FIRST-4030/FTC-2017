package org.firstinspires.ftc.teamcode.sensors.gyro;

public interface Gyro {
    boolean isAvailable();

    boolean isReady();

    int getHeading();

    int getRaw();

    void setOffset(int offset);
}
