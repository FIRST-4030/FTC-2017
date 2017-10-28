package org.firstinspires.ftc.teamcode.wheels;

import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class TankDrive {
    private static final int MIN_MOTORS = 2;

    private TankMotor[] motors = null;
    private boolean disabled = true;
    private boolean teleop = false;
    private double speedScale = 1.0;
    private int encoderIndex = 0;
    private double encoderScale = 1.0;

    public TankDrive(HardwareMap map, TankMotor[] motors, int index) {
        this(map, motors, index, 1.0);
    }

    public TankDrive(HardwareMap map, TankMotor[] motors, int index, double scale) {
        this.teleop = false;
        this.encoderIndex = index;
        this.encoderScale = scale;
        try {
            if (motors.length < MIN_MOTORS) {
                throw new ArrayIndexOutOfBoundsException("TankDrive must configure at least " +
                        MIN_MOTORS + " motors: " + motors.length);
            }
            this.motors = motors;
            for (TankMotor motor : this.motors) {
                motor.motor = map.dcMotor.get(motor.name);
                if (motor.reverse) {
                    motor.motor.setDirection(DcMotorSimple.Direction.REVERSE);
                }
            }
            this.disabled = false;
        } catch (Exception e) {
            this.motors = null;
            this.disabled = true;
        }
    }

    public boolean isAvailable() {
        return motors != null;
    }

    public int getEncoder() {
        return getEncoder(encoderIndex);
    }

    public int getEncoder(int index) {
        if (!isAvailable()) {
            return 0;
        }
        if (index < 0 || index >= motors.length) {
            throw new ArrayIndexOutOfBoundsException("Invalid TankMotors index: " + index);
        }
        return (int) ((double) motors[index].motor.getCurrentPosition() * encoderScale);
    }

    public void setSpeed(double speed) {
        if (isDisabled()) {
            return;
        }
        for (TankMotor motor : motors) {
            motor.motor.setPower(speed * speedScale);
        }
    }

    public void setSpeed(double speed, MotorSide side) {
        if (isDisabled()) {
            return;
        }
        for (TankMotor motor : motors) {
            if (motor.side == side) {
                motor.motor.setPower(speed * speedScale);
            }
        }
    }

    public void stop() {
        if (!isAvailable()) {
            return;
        }
        for (TankMotor motor : motors) {
            motor.motor.setPower(0.0d);
        }
    }

    public int numMotors() {
        if (!isAvailable()) {
            return 0;
        }
        return motors.length;
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
