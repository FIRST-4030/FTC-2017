package org.firstinspires.ftc.teamcode.driveto;

import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.utils.Heading;

public class PID {
    public final double P;
    public final double I;
    public final double D;

    public long timestamp;
    public double last;
    public double error;
    public double accumulated;
    public double differential;
    public double rate;
    public double target;

    public PID() {
        this(1.0d, 0.0d, 0.0d);
    }

    public PID(PIDParams params) {
        this(params.P, params.I, params.D);
    }

    public PID(double p, double i, double d) {
        this.P = p;
        this.I = i;
        this.D = d;
        this.target = 0.0d;
        reset();
    }

    public void setTarget(double target) {
        this.target = target;
    }

    public void reset() {
        this.timestamp = System.currentTimeMillis();
        this.last = 0.0d;
        this.error = 0.0d;
        this.accumulated = 0.0d;
        this.differential = 0.0d;
        this.rate = 0.0d;
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
        double dt = now - timestamp;
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
