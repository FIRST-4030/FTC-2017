package org.firstinspires.ftc.teamcode.driveto;

public class PID {
    public double P;
    public double I;
    public double D;

    public long timestamp = 0;
    public double last = 0.0d;
    public double error = 0.0d;
    public double accumulated = 0.0d;
    public double differential = 0.0d;
    public double rate = 0.0d;
    public double target = 0.0d;

    public PID() {
        this(1.0d, 0.0d, 0.0d);
    }

    public PID(double p, double i, double d) {
        this.P = p;
        this.I = i;
        this.D = d;
        this.timestamp = System.currentTimeMillis();
    }

    public void setTarget(double target) {
        this.target = target;
    }

    public double run(double actual) {
        input(actual);
        return output();
    }

    public void input(double actual) {
        long now = System.currentTimeMillis();
        double dt = timestamp - now;
        double err = target - actual;
        double r = (actual - last) / dt;
        double diff = (err - error) / dt;
        double acc = accumulated + (err / dt);

        last = actual;
        rate = r;
        error = err;
        accumulated = acc;
        differential = diff;
        timestamp = now;
    }

    public double output() {
        return (P * error) + (I * accumulated) + (D * differential);
    }
}
