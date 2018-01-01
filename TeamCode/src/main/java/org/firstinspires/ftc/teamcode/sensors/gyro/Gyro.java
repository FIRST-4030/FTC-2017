package org.firstinspires.ftc.teamcode.sensors.gyro;

import org.firstinspires.ftc.teamcode.utils.Available;

public interface Gyro extends Available {
    boolean isReady();

    float getHeading();

    float getRaw();

    void setOffset(float offset);
}
