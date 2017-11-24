package org.firstinspires.ftc.teamcode.actuators;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.utils.Available;

public class ServoFTC implements Available {
    private Servo servo;
    private double min = 0.0d;
    private double max = 1.0d;

    public ServoFTC(HardwareMap map, Telemetry telemetry, ServoConfig config) {
        if (config == null) {
            telemetry.log().add(this.getClass().getName() + ": Null config");
            return;
        }
        if (config.name == null || config.name.isEmpty()) {
            throw new IllegalArgumentException(this.getClass().getName() + ": Null/empty name");
        }
        try {
            servo = map.servo.get(config.name);
            if (config.reverse) {
                servo.setDirection(Servo.Direction.REVERSE);
            }
            this.min = config.min;
            this.max = config.max;
        } catch (Exception e) {
            servo = null;
            telemetry.log().add(this.getClass().getName() + "No such device: " + config.name);
        }
    }

    public boolean isAvailable() {
        return servo != null;
    }

    public void setPosition(double position) {
        if (position < min) {
            position = min;
        } else if (position > max) {
            position = max;
        }
        setPositionRaw(position);
    }

    public void setPositionRaw(double position) {
        if (!isAvailable()) {
            return;
        }
        servo.setPosition(position);
    }

    public double getPostion() {
        if (!isAvailable()) {
            return 0.0d;
        }
        return servo.getPosition();
    }

    public void min() {
        setPositionRaw(min);
    }

    public void max() {
        setPosition(max);
    }

    public void toggle() {
        if (getPostion() == max) {
            min();
        } else {
            max();
        }
    }
}
