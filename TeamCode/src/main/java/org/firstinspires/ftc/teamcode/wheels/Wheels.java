package org.firstinspires.ftc.teamcode.wheels;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.driveto.RatePID;
import org.firstinspires.ftc.teamcode.utils.Available;

public interface Wheels extends Available {
    void stop();

    void setSpeed(float speed);

    void setSpeed(float speed, MOTOR_SIDE side);

    void setPowerRaw(float speed);

    void setPowerRaw(float speed, MOTOR_SIDE side);

    float getTicksPerMM();

    float getTicksPerMM(MOTOR_SIDE side);

    float getTicksPerMM(MOTOR_SIDE side, MOTOR_END end);

    RatePID getPID(MOTOR_SIDE side, MOTOR_END end);

    int getEncoder();

    int getEncoder(int index);

    int getEncoder(MOTOR_SIDE side);

    int getEncoder(MOTOR_SIDE side, MOTOR_END end);

    void resetEncoder();

    void resetEncoder(int index);

    void resetEncoder(MOTOR_SIDE side);

    void resetEncoder(MOTOR_SIDE side, MOTOR_END end);

    float getRate();

    float getRate(MOTOR_SIDE side);

    float getRate(MOTOR_SIDE side, MOTOR_END end);

    void loop(Gamepad pad);

    boolean isTeleop();

    void setTeleop(boolean enabled);

    void setSpeedScale(float scale);
}
