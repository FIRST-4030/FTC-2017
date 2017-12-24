package org.firstinspires.ftc.teamcode.driveto;

public class RatePID extends PID {
    private PID displacement = new PID();

    @Override
    public void input(double actual) {
        this.input(actual, false);
    }

    @Override
    public void inputRotational(double actual) {
        this.input(actual, true);
    }

    @Override
    protected void input(double actual, boolean rotational) {
        displacement.input(actual, rotational);
        super.input(displacement.rate, rotational);
    }
}
