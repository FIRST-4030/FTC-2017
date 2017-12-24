package org.firstinspires.ftc.teamcode.driveto;

public class PIDParams {
    public double P;
    public double I;
    public double D;
    public double maxRate;
    public double ticksPerMM;

    public PIDParams(double p, double i, double d, double maxRate, double ticksPerMM) {
        this.P = p;
        this.I = i;
        this.D = d;
        this.maxRate = maxRate;
        this.ticksPerMM = ticksPerMM;
    }
}
