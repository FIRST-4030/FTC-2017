package org.firstinspires.ftc.teamcode.wheels;

import org.firstinspires.ftc.teamcode.config.Config;

public class WheelsConfig implements Config {
    public WheelMotor[] motors;
    public DRIVE_TYPE type;
    public double scale;

    public WheelsConfig(DRIVE_TYPE type, WheelMotor[] motors, double scale) {
        if (motors == null) {
            throw new IllegalArgumentException(this.getClass().getName() + ": No motors provided");
        }

        int minMotors = 0;
        switch (type) {
            case TANK:
                minMotors = 2;
                break;
            case MECANUM:
                minMotors = 4;
                break;
        }
        if (motors.length < minMotors) {
            throw new IllegalArgumentException(this.getClass().getName() +
                    ": Not enough motors configured " + motors.length + "/" + minMotors);
        }

        this.type = type;
        this.motors = motors;
        this.scale = scale;
    }
}