package org.firstinspires.ftc.teamcode.actuators;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.utils.Available;

public class Motor implements Available {
    private DcMotor motor = null;
    private boolean enabled = true;
    private int offset = 0;

    public Motor(HardwareMap map, Telemetry telemetry, MotorConfig config) {
        if (config == null) {
            telemetry.log().add(this.getClass().getSimpleName() + ": Null config");
            return;
        }
        if (config.name == null || config.name.isEmpty()) {
            telemetry.log().add(this.getClass().getSimpleName() + ": Null/empty name");
            return;
        }
        try {
            motor = map.dcMotor.get(config.name);
            if (config.reverse) {
                motor.setDirection(DcMotor.Direction.REVERSE);
            }
        } catch (Exception e) {
            telemetry.log().add(this.getClass().getSimpleName() + "No such device: " + config.name);
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
        return enabled && (motor != null);
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
        return motor.getCurrentPosition() + offset;
    }

    public void resetEncoder() {
        DcMotor.RunMode mode;
        try {
            mode = motor.getMode();
        } catch (Exception e) {
            mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER;
        }
        setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        setMode(mode);
        offset = -getEncoder();
    }

    public void setMode(DcMotor.RunMode mode) {
        if (!isAvailable()) {
            return;
        }
        motor.setMode(mode);
    }
}
