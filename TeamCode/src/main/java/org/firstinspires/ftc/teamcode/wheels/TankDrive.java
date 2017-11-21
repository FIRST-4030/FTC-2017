package org.firstinspires.ftc.teamcode.wheels;

import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class TankDrive {
    private TankConfig config = null;
    private boolean disabled = true;
    private boolean teleop = false;
    private double speedScale = 1.0f;
    private int[] offsets;

    public TankDrive(HardwareMap map, Telemetry telemetry, TankConfig config) {
        for (TankMotor motor : config.motors) {
            if (motor == null) {
                telemetry.log().add(this.getClass().getName() + ": Null motor");
                break;
            }
            if (motor.name == null || motor.name.isEmpty()) {
                throw new IllegalArgumentException(this.getClass().getName() + ": Null motor or null/empty motor name");
            }
            try {
                motor.motor = map.dcMotor.get(motor.name);
                if (motor.reverse) {
                    motor.motor.setDirection(DcMotorSimple.Direction.REVERSE);
                }
            } catch (Exception e) {
                telemetry.log().add(this.getClass().getName() + ": No such device: " + motor.name);
                return;
            }
        }
        this.offsets = new int[config.motors.length];
        for (int i = 0; i < config.motors.length; i++) {
            offsets[i] = 0;
        }
        this.config = config;
        this.disabled = false;
    }

    public boolean isAvailable() {
        return config != null;
    }

    public void resetEncoder() {
        resetEncoder(config.index);
    }

    public void resetEncoder(int index) {
        offsets[index] = -getEncoder(index);
    }

    public int getEncoder() {
        return getEncoder(config.index);
    }

    public int getEncoder(int index) {
        if (!isAvailable()) {
            return 0;
        }
        if (index < 0 || index >= config.motors.length) {
            throw new ArrayIndexOutOfBoundsException(this.getClass().getName() + ": Invalid index: " + index);
        }
        return (int) ((double) (config.motors[index].motor.getCurrentPosition() + offsets[index]) * config.scale);
    }

    public void setSpeed(double speed) {
        if (isDisabled()) {
            return;
        }
        for (TankMotor motor : config.motors) {
            motor.motor.setPower(speed * speedScale);
        }
    }

    public void setSpeed(double speed, MotorSide side) {
        if (isDisabled()) {
            return;
        }
        for (TankMotor motor : config.motors) {
            if (motor.side == side) {
                motor.motor.setPower(speed * speedScale);
            }
        }
    }

    public void stop() {
        if (!isAvailable()) {
            return;
        }
        for (TankMotor motor : config.motors) {
            motor.motor.setPower(0.0d);
        }
    }

    public int numMotors() {
        if (!isAvailable()) {
            return 0;
        }
        return config.motors.length;
    }

    public boolean isDisabled() {
        return !isAvailable() || this.disabled;
    }

    public void setDisabled(boolean disabled) {
        if (!this.disabled && disabled) {
            stop();
        }
        this.disabled = disabled;
    }

    public boolean isTeleop() {
        return this.teleop;
    }

    public void setTeleop(boolean enabled) {
        if (this.teleop != enabled) {
            stop();
        }
        this.teleop = enabled;
    }

    public void setSpeedScale(double scale) {
        this.speedScale = scale;
    }

    public void loop(Gamepad pad) {
        if (isDisabled() || !isTeleop() || pad == null) {
            return;
        }

        float left = cleanJoystick(pad.left_stick_y);
        this.setSpeed(left, MotorSide.LEFT);

        float right = cleanJoystick(pad.right_stick_y);
        this.setSpeed(right, MotorSide.RIGHT);
    }

    private float cleanJoystick(float power) {
        power = com.qualcomm.robotcore.util.Range.clip(power, -1f, 1f);
        if (power < 0.1 && power > -0.1) {
            return 0;
        } else {
            return power;
        }
    }
}
