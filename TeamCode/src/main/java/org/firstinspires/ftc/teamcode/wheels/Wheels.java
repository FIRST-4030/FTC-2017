package org.firstinspires.ftc.teamcode.wheels;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.utils.Available;

public interface Wheels extends Available {
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
