package org.firstinspires.ftc.teamcode.actuators;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Motor {
    private DcMotor motor;
    private boolean enabled = true;

    public Motor(HardwareMap map, MotorConfig config) {
        try {
            motor = map.dcMotor.get(config.name);
            if (config.reverse) {
                motor.setDirection(DcMotor.Direction.REVERSE);
            }
        } catch (Exception e) {
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
