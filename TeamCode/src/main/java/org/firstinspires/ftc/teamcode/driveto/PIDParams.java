package org.firstinspires.ftc.teamcode.driveto;

public class PIDParams {
    public float P;
    public float I;
    public float D;

    // If set, limit the accumulator value to the range ±maxAccumulator
    public Float maxAccumulator;
    // If true, reset the accumulated error whenever the error sign changes
    // This is useful when the input() values are not rate-based (e.g. raw displacement or heading)
    public boolean resetAccumulatorOnErrorSignChange;
    // If true, reset the accumulator error whenever the target sign changes
    // This is usually desirable but might interfere with non-rate, non-normalized input() values
    public boolean resetAccumulatorOnTargetSignChange;

    public PIDParams() {
        this(0.1f, 0.01f, 0.0f);
    }

    public PIDParams(float p, float i, float d) {
        this(p, i, d,
                null,
                false,
                true);
    }

    public PIDParams(float p, float i, float d,
                     Float maxAccumulator,
                     boolean resetAccumulatorOnErrorSignChange,
                     boolean resetAccumulatorOnTargetSignChange) {
        this.P = p;
        this.I = i;
        this.D = d;
        this.maxAccumulator = maxAccumulator;
        this.resetAccumulatorOnErrorSignChange = resetAccumulatorOnErrorSignChange;
        this.resetAccumulatorOnTargetSignChange = resetAccumulatorOnTargetSignChange;
    }
}
