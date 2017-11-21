package org.firstinspires.ftc.teamcode.wheels;

public class TankConfig {
    private static final int MIN_MOTORS = 2;

    public TankMotor[] motors;
    public int index;
    public double scale;

    public TankConfig(TankMotor[] motors) {
        this(motors, 0, 1.0d);
    }

    public TankConfig(TankMotor[] motors, int index, double scale) {
        if (motors == null || motors.length < MIN_MOTORS) {
            throw new IllegalArgumentException(this.getClass().getName() +
                    " must configure at least " + MIN_MOTORS + " motors");
        }
        this.motors = motors;
        this.index = index;
        this.scale = scale;
    }
}