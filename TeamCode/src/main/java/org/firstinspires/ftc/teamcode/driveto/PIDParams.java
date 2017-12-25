package org.firstinspires.ftc.teamcode.driveto;

public class PIDParams {
    public final double P;
    public final double I;
    public final double D;
    public final double maxRate;
    public final double ticksPerMM;

    public PIDParams(double p, double i, double d) {
        this(p, i, d, 1.0d, 1.0d);
    }

    public PIDParams(double p, double i, double d, double maxRate, double ticksPerMM) {
        this.P = p;
        this.I = i;
        this.D = d;
        this.maxRate = maxRate;
        this.ticksPerMM = ticksPerMM;
    }
}
