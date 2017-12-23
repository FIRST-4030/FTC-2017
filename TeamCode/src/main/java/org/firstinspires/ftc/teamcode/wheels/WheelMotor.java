package org.firstinspires.ftc.teamcode.wheels;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.utils.Available;

public class WheelMotor implements Available {
    public final MOTOR_SIDE side;
    public final MOTOR_END end;
    public final String name;
    public DcMotor motor;
    public final boolean reverse;
    public final boolean encoder;

    public WheelMotor(String name, MOTOR_SIDE side, boolean reverse, boolean encoder) {
        this(name, side, MOTOR_END.FRONT, reverse, encoder);
    }

    public WheelMotor(String name, MOTOR_SIDE side, MOTOR_END end, boolean reverse, boolean encoder) {
        this.name = name;
        this.side = side;
        this.end = end;
        this.motor = null;
        this.reverse = reverse;
        this.encoder = encoder;
    }

    public boolean isAvailable() {
        return this.motor != null;
    }
}
