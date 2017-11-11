package org.firstinspires.ftc.teamcode.actuators;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Motor {
    private DcMotor motor;
    private boolean enabled = true;

    public Motor(HardwareMap map, MotorConfig config, Telemetry telemetry) {
        if (config == null || config.name == null || config.name.isEmpty()) {
            throw new IllegalArgumentException(this.getClass().getName() + ": Null config or null/empty name");
        }
        try {
            motor = map.dcMotor.get(config.name);
            if (config.reverse) {
                motor.setDirection(DcMotor.Direction.REVERSE);
            }
        } catch (Exception e) {
            if (telemetry != null) {
                telemetry.log().add(this.getClass().getName() + "No such device: " + config.name);
            }
            motor = null;
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            this.stop();
        }
    }

    public boolean isAvailable() {
        return enabled & (motor != null);
    }

    public void setPower(double power) {
        if (!isAvailable()) {
            return;
        }
        motor.setPower(power);
    }

    public void stop() {
        setPower(0);
    }

    public int getEncoder() {
        if (!isAvailable()) {
            return 0;
        }
        return motor.getCurrentPosition();
    }
}
