package org.firstinspires.ftc.teamcode.wheels;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.utils.Available;

public interface Wheels extends Available {
    void stop();

    void setSpeed(double speed);

    void setSpeed(double speed, MOTOR_SIDE side);

    void setPowerRaw(double speed);

    void setPowerRaw(double speed, MOTOR_SIDE side);

    double getTicksPerMM();

    double getTicksPerMM(MOTOR_SIDE side);

    double getTicksPerMM(MOTOR_SIDE side, MOTOR_END end);

    int getEncoder();

    int getEncoder(int index);

    int getEncoder(MOTOR_SIDE side);

    int getEncoder(MOTOR_SIDE side, MOTOR_END end);

    void resetEncoder();

    void resetEncoder(int index);

    void resetEncoder(MOTOR_SIDE side);

    void resetEncoder(MOTOR_SIDE side, MOTOR_END end);

    double getRate();

    double getRate(MOTOR_SIDE side);

    double getRate(MOTOR_SIDE side, MOTOR_END end);

    void loop(Gamepad pad);

    boolean isTeleop();

    void setTeleop(boolean enabled);

    void setSpeedScale(double scale);
}
