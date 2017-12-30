package org.firstinspires.ftc.teamcode.driveto;

import org.firstinspires.ftc.teamcode.utils.Heading;

public class PID {
    public final PIDParams params;

    public long timestamp;
    public float last;
    public float error;
    public float accumulated;
    public float differential;
    public float rate;
    public float target;

    // If set, limit the accumulator value to the range ±maxAccumulator
    public Float maxAccumulator;
    // If true, reset the accumulated error whenever the error sign changes
    // This is useful when the input() values are not rate-based (e.g. raw displacement or heading)
    public boolean resetAccumulatorOnErrorSignChange;
    // If true, reset the accumulator error whenever the target sign changes
    // This is usually desirable but might interfere with non-rate, non-normalized input() values
    public boolean resetAccumulatorOnTargetSignChange;

    public PID() {
        this(new PIDParams(0.1f, 0.01f, 0.0f));
    }

    public PID(PIDParams params) {
        this.params = params;
        this.target = 0.0f;
        this.maxAccumulator = null;
        this.resetAccumulatorOnErrorSignChange = false;
        this.resetAccumulatorOnTargetSignChange = true;
        reset();
    }

    public void setTarget(float newTarget) {
        // If the target sign changes our accumulator is probably invalid
        if (resetAccumulatorOnTargetSignChange &&
                Math.signum(target) != Math.signum(newTarget)) {
            accumulated = 0.0f;
        }
        target = newTarget;
    }

    public void reset() {
        this.timestamp = System.currentTimeMillis();
        this.last = 0.0f;
        this.error = 0.0f;
        this.accumulated = 0.0f;
        this.differential = 0.0f;
        this.rate = 0.0f;
    }

    public float run(float actual) {
        input(actual);
        return output();
    }

    public float output() {
        return (params.P * error) + (params.I * accumulated) + (params.D * differential);
    }

    public void input(float actual) {
        input(actual, false);
    }

    public void inputRotational(float actual) {
        input(actual, true);
    }

    protected void input(float actual, boolean rotational) {
        long now = System.currentTimeMillis();
        float dt = now - timestamp;
        float r = (actual - last) / dt;

        float err = target - actual;
        // Errors in a rotational context are always between -180 and 180
        if (rotational) {
            err = Heading.normalizeErr(err);
        }

        float acc = accumulated + (err / dt);
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

        float diff = (err - error) / dt;

        // Save new values
        last = actual;
        rate = r;
        error = err;
        accumulated = acc;
        differential = diff;
        timestamp = now;
    }
}
