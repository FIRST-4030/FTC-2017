package org.firstinspires.ftc.teamcode.driveto;

import org.firstinspires.ftc.teamcode.utils.Heading;

public class PID {
    public final double P;
    public final double I;
    public final double D;

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
        reset();
    }

    public void setTarget(double target) {
        this.target = target;
    }

    public void reset() {
        this.error = 0.0d;
        this.accumulated = 0.0d;
        this.differential = 0.0d;
        this.timestamp = System.currentTimeMillis();
    }

    public double run(double actual) {
        input(actual);
        return output();
    }

    public void input(double actual) {
        input(actual, false);
    }

    public void inputRotational(double actual) {
        input(actual, true);
    }

    protected void input(double actual, boolean rotational) {
        long now = System.currentTimeMillis();
        double dt = timestamp - now;
        double err = target - actual;
        if (rotational) {
            err = Heading.normalizeErr(err);
        }
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
