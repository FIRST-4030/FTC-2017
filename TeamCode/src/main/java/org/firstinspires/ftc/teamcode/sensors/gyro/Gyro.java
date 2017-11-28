package org.firstinspires.ftc.teamcode.sensors.gyro;

import org.firstinspires.ftc.teamcode.utils.Available;

public interface Gyro extends Available {
    boolean isReady();

    int getHeading();

    int getRaw();

    void setOffset(int offset);
}
