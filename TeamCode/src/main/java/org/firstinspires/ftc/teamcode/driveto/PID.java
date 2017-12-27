package org.firstinspires.ftc.teamcode.driveto;

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

    // If set, limit the accumulator value to the range ±maxAccumulator
    public Double maxAccumulator;
    // If true, reset the accumulated error whenever the error sign changes
    // This is useful when the input() values are not rate-based (e.g. raw displacement or heading)
    public boolean resetAccumulatorOnErrorSignChange;
    // If true, reset the accumulator error whenever the target sign changes
    // This is usually desirable but might interfere with non-rate, non-normalized input() values
    public boolean resetAccumulatorOnTargetSignChange;

    public PID() {
        this(0.1d, 0.01d, 0.0d);
    }

    public PID(PIDParams params) {
        this(params.P, params.I, params.D);
    }

    public PID(double p, double i, double d) {
        this.P = p;
        this.I = i;
        this.D = d;
        this.target = 0.0d;
        this.maxAccumulator = null;
        this.resetAccumulatorOnErrorSignChange = false;
        this.resetAccumulatorOnTargetSignChange = true;
        reset();
    }

    public void setTarget(double newTarget) {
        // If the target sign changes our accumulator is probably invalid
        if (resetAccumulatorOnTargetSignChange &&
                Math.signum(target) != Math.signum(newTarget)) {
            accumulated = 0;
        }
        target = newTarget;
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
        double r = (actual - last) / dt;

        double err = target - actual;
        // Errors in a rotational context are always between -180 and 180
        if (rotational) {
            err = Heading.normalizeErr(err);
        }

        double acc = accumulated + (err / dt);
        // Limit the accumulator to avoid wind-up errors
        if (maxAccumulator != null) {
            if (Math.abs(accumulated) > maxAccumulator) {
                accumulated = Math.copySign(maxAccumulator, accumulated);
            }
        }
        // Reset the accumulator when the error sign changes, if requested
        if (resetAccumulatorOnErrorSignChange && Math.signum(err) != Math.signum(error)) {
            acc = 0;
        }

        double diff = (err - error) / dt;

        // Save new values
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
