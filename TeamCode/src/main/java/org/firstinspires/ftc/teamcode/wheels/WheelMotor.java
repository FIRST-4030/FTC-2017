package org.firstinspires.ftc.teamcode.wheels;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.driveto.PIDParams;
import org.firstinspires.ftc.teamcode.utils.Available;

public class WheelMotor implements Available {
    public final MOTOR_SIDE side;
    public final MOTOR_END end;
    public final String name;
    public DcMotor motor;
    public final boolean reverse;
    public final boolean encoder;
    public final PIDParams pid;
    public final float ticksPerMM;
    public final float maxRate;

    public WheelMotor(String name, MOTOR_SIDE side, boolean reverse) {
        this(name, side, MOTOR_END.FRONT, reverse);
    }

    public WheelMotor(String name, MOTOR_SIDE side, MOTOR_END end, boolean reverse) {
        this(name, side, end, reverse, null, 1.0f, 1.0f);
    }

    public WheelMotor(String name, MOTOR_SIDE side, boolean reverse,
                      PIDParams pid, float ticksPerMM, float maxRate) {
        this(name, side, MOTOR_END.FRONT, reverse, pid, ticksPerMM, maxRate);
    }

    public WheelMotor(String name, MOTOR_SIDE side, MOTOR_END end, boolean reverse,
                      PIDParams pid, float ticksPerMM, float maxRate) {
        this.name = name;
        this.side = side;
        this.end = end;
        this.motor = null;
        this.reverse = reverse;
        this.pid = pid;
        this.encoder = (pid != null);
        this.ticksPerMM = ticksPerMM;
        this.maxRate = maxRate;
    }

    public boolean isAvailable() {
        return this.motor != null;
    }
}
