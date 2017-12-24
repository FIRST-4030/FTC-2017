package org.firstinspires.ftc.teamcode.wheels;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class TankDrive implements Wheels {
    protected WheelsConfig config = null;
    private boolean teleop = false;
    protected double speedScale = 1.0f;
    private int[] offsets;
    private int[] last;
    private long timestamp;

    public TankDrive(HardwareMap map, Telemetry telemetry, WheelsConfig config) {
        for (WheelMotor motor : config.motors) {
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
        this.last = new int[config.motors.length];
        this.timestamp = System.currentTimeMillis();
        this.config = config;
        resetEncoder();
    }

    public boolean isAvailable() {
        return config != null;
    }

    public void resetEncoder() {
        resetEncoder(null, null);
    }

    public void resetEncoder(MOTOR_SIDE side) {
        resetEncoder(side, null);
    }

    public void resetEncoder(MOTOR_SIDE side, MOTOR_END end) {
        if (!isAvailable()) {
            return;
        }
        for (int i = 0; i < config.motors.length; i++) {
            if (config.motors[i].encoder &&
                    (end == null || config.motors[i].end == end) &&
                    (side == null || config.motors[i].side == side)) {
                resetEncoder(i);
            }
        }
    }

    public void resetEncoder(int index) {
        if (!isAvailable()) {
            return;
        }
        if (!config.motors[index].encoder) {
            return;
        }

        DcMotor.RunMode mode;
        try {
            mode = config.motors[index].motor.getMode();
        } catch (Exception e) {
            mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER;
        }
        setMode(index, DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        setMode(index, mode);
        offsets[index] = -getEncoder(index);
    }

    public void setMode(int index, DcMotor.RunMode mode) {
        if (!isAvailable()) {
            return;
        }
        config.motors[index].motor.setMode(mode);
    }

    public int getEncoder() {
        return getEncoder(null, null);
    }

    public int getEncoder(MOTOR_SIDE side) {
        return getEncoder(side, null);
    }

    public int getEncoder(MOTOR_SIDE side, MOTOR_END end) {
        int position = 0;
        boolean found = false;
        for (int i = 0; i < config.motors.length; i++) {
            if (config.motors[i].encoder &&
                    (end == null || config.motors[i].end == end) &&
                    (side == null || config.motors[i].side == side)) {
                position = getEncoder(i);
                found = true;
                break;
            }
        }
        if (!found) {
            System.err.println(this.getClass().getName() +
                    ": No encoder for SIDE/END: " +
                    (side != null ? side : "<any>") +
                    "/" +
                    (end != null ? end : "<any>"));
        }
        return position;
    }

    public int getEncoder(int index) {
        if (!isAvailable()) {
            return 0;
        }
        if (index < 0 || index >= config.motors.length) {
            throw new ArrayIndexOutOfBoundsException(this.getClass().getName() + ": Invalid index: " + index);
        }
        if (!config.motors[index].encoder) {
            System.err.println(this.getClass().getName() + ": No encoder on wheel: " + index);
            return 0;
        }
        return (int) ((double) (config.motors[index].motor.getCurrentPosition() + offsets[index]) * config.scale);
    }

    public double getRate() {
        return getRate(null, null);
    }

    public double getRate(MOTOR_SIDE side) {
        return getRate(side, null);
    }

    public double getRate(MOTOR_SIDE side, MOTOR_END end) {
        // Calculate the encoder tick rate since the last call to getRate()
        long now = System.currentTimeMillis();
        int current = getEncoder(side, end);
        double rate = (current - encoderLast(side, end)) / (double) (now - timestamp);

        // Update all encoders and the timestamp
        for (int i = 0; i < config.motors.length; i++) {
            last[i] = getEncoder(i);
        }
        timestamp = now;
        return rate;
    }

    private int encoderLast(MOTOR_SIDE side, MOTOR_END end) {
        int position = 0;
        boolean found = false;
        for (int i = 0; i < config.motors.length; i++) {
            if (config.motors[i].encoder &&
                    (end == null || config.motors[i].end == end) &&
                    (side == null || config.motors[i].side == side)) {
                position = last[i];
                found = true;
                break;
            }
        }
        if (!found) {
            System.err.println(this.getClass().getName() +
                    ": No encoder for SIDE/END: " +
                    (side != null ? side : "<any>") +
                    "/" +
                    (end != null ? end : "<any>"));
        }
        return position;
    }

    public void setSpeed(double speed) {
        for (MOTOR_SIDE side : MOTOR_SIDE.values()) {
            setSpeed(speed, side);
        }
    }

    public void setSpeed(double speed, MOTOR_SIDE side) {
        setSpeedRaw(speed, side);
    }

    public void setSpeedRaw(double speed) {
        for (MOTOR_SIDE side : MOTOR_SIDE.values()) {
            setSpeedRaw(speed, side);
        }
    }

    public void setSpeedRaw(double speed, MOTOR_SIDE side) {
        if (!isAvailable()) {
            return;
        }
        for (WheelMotor motor : config.motors) {
            if (motor.side == side) {
                motor.motor.setPower(speed * speedScale);
            }
        }
    }

    public void stop() {
        if (!isAvailable()) {
            return;
        }
        for (WheelMotor motor : config.motors) {
            motor.motor.setPower(0.0d);
        }
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
        if (!isAvailable() || !isTeleop() || pad == null) {
            return;
        }

        // Negative is forward; this is typically the opposite of native motor config
        float left = cleanJoystick(-pad.left_stick_y);
        this.setSpeed(left, MOTOR_SIDE.LEFT);

        float right = cleanJoystick(-pad.right_stick_y);
        this.setSpeed(right, MOTOR_SIDE.RIGHT);
    }

    protected float cleanJoystick(float power) {
        power = com.qualcomm.robotcore.util.Range.clip(power, -1f, 1f);
        if (power < 0.1 && power > -0.1) {
            return 0;
        } else {
            return power;
        }
    }
}
