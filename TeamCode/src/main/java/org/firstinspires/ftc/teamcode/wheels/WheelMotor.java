package org.firstinspires.ftc.teamcode.wheels;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.utils.Available;

public class WheelMotor implements Available {
    public MOTOR_SIDE side;
    public MOTOR_END end;
    public String name;
    public DcMotor motor;
    public boolean reverse;

    public WheelMotor(String name, MOTOR_SIDE side) {
        this(name, side, false);
    }

    public WheelMotor(String name, MOTOR_SIDE side, boolean reverse) {
        this(name, side, MOTOR_END.FRONT, reverse);
    }

    public WheelMotor(String name, MOTOR_SIDE side, MOTOR_END end) {
        this(name, side, end, false);
    }

    public WheelMotor(String name, MOTOR_SIDE side, MOTOR_END end, boolean reverse) {
        this.name = name;
        this.side = side;
        this.end = end;
        this.motor = null;
        this.reverse = reverse;
    }

    public boolean isAvailable() {
        return this.motor != null;
    }
}
