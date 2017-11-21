package org.firstinspires.ftc.teamcode.wheels;

import com.qualcomm.robotcore.hardware.Gamepad;

public interface Wheels {
    boolean isAvailable();

    void stop();

    void setSpeed(double speed);

    void setSpeed(double speed, MOTOR_SIDE side);

    int getEncoder();

    void resetEncoder();

    void loop(Gamepad pad);

    boolean isTeleop();

    void setTeleop(boolean enabled);

    void setSpeedScale(double scale);
}
