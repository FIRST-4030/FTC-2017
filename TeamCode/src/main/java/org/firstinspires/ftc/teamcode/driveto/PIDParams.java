package org.firstinspires.ftc.teamcode.driveto;

public class PIDParams {
    public float P;
    public float I;
    public float D;
    public float maxRate;
    public float ticksPerMM;

    public PIDParams(float p, float i, float d) {
        this(p, i, d, 1.0f, 1.0f);
    }

    public PIDParams(float p, float i, float d, float maxRate, float ticksPerMM) {
        this.P = p;
        this.I = i;
        this.D = d;
        this.maxRate = maxRate;
        this.ticksPerMM = ticksPerMM;
    }
}
