package org.firstinspires.ftc.teamcode.driveto;

public class RatePID extends PID {
    private final PID displacement = new PID();

    public RatePID(float p, float i, float d) {
        super(new PIDParams(p, i, d));
    }

    public RatePID(PIDParams params) {
        super(params);
    }

    @Override
    public void input(float actual) {
        this.input(actual, false);
    }

    @Override
    public void inputRotational(float actual) {
        this.input(actual, true);
    }

    @Override
    protected void input(float actual, boolean rotational) {
        displacement.input(actual, rotational);
        super.input(displacement.rate, rotational);
    }
}
