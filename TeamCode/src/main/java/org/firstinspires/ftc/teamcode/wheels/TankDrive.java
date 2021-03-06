package org.firstinspires.ftc.teamcode.wheels;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.actuators.MotorConfig;
import org.firstinspires.ftc.teamcode.driveto.RatePID;
import org.firstinspires.ftc.teamcode.utils.Round;

public class TankDrive implements Wheels {
    private static final boolean DEBUG = false;
    private static final float JOYSTICK_DEADZONE = 0.1f;
    private static final float SPEED_DEADZONE = JOYSTICK_DEADZONE * 0.85f;

    protected WheelsConfig config = null;
    protected final Telemetry telemetry;
    protected float speedScale = 1.0f;
    private boolean teleop = false;
    private RatePID[] pids;

    public TankDrive(HardwareMap map, Telemetry telemetry, WheelsConfig config) {
        this.telemetry = telemetry;
        for (WheelMotor wheelMotor : config.motors) {
            if (wheelMotor == null) {
                telemetry.log().add(this.getClass().getSimpleName() + ": Null motor");
                break;
            }
            wheelMotor.motor = new Motor(map, telemetry, wheelMotor);
            if (!wheelMotor.motor.isAvailable()) {
                return;
            }
        }
        this.pids = new RatePID[config.motors.length];
        for (int i = 0; i < config.motors.length; i++) {
            RatePID pid = null;
            if (config.motors[i].encoder) {
                pid = new RatePID(config.motors[i].pid);
            }
            this.pids[i] = pid;
        }
        this.config = config;
        resetEncoder();
    }

    public RatePID getPID(MOTOR_SIDE side, MOTOR_END end) {
        Integer index = findEncoderIndex(side, end);
        if (index == null) {
            return null;
        }
        return pids[index];
    }

    public boolean isAvailable() {
        return config != null;
    }

    private void checkMotorIndex(int index) {
        if (index < 0 || index >= config.motors.length) {
            throw new ArrayIndexOutOfBoundsException(this.getClass().getSimpleName() + ": Invalid index: " + index);
        }
    }

    public DcMotor.RunMode getMode(int index) {
        if (!isAvailable()) {
            return Motor.DEFAULT_MODE;
        }
        checkMotorIndex(index);
        return config.motors[index].motor.getMode();
    }

    public void setMode(int index, DcMotor.RunMode mode) {
        if (!isAvailable()) {
            return;
        }
        checkMotorIndex(index);
        config.motors[index].motor.setMode(mode);
    }

    public void resetEncoder(int index) {
        if (!checkEncoderIndex(index)) {
            return;
        }
        config.motors[index].motor.resetEncoder();
    }

    public int getEncoder(int index) {
        if (!checkEncoderIndex(index)) {
            return 0;
        }
        return (config.motors[index].motor.getEncoder());
    }

    private boolean checkEncoderIndex(int index) {
        if (!isAvailable()) {
            return false;
        }
        checkMotorIndex(index);
        if (!config.motors[index].encoder) {
            telemetry.log().add("No encoder on motor: " + index);
            return false;
        }
        return true;
    }

    private Integer findEncoderIndex(MOTOR_SIDE side, MOTOR_END end) {
        if (!isAvailable()) {
            return null;
        }
        Integer motor = null;
        for (int i = 0; i < config.motors.length; i++) {
            if (config.motors[i].encoder &&
                    (end == null || config.motors[i].end == end) &&
                    (side == null || config.motors[i].side == side)) {
                motor = i;
                break;
            }
        }
        if (motor == null) {
            telemetry.log().add("No encoder for SIDE/END: " +
                    (side != null ? side : "<any>") + "/" +
                    (end != null ? end : "<any>"));
        }
        return motor;
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

    public float getTicksPerMM() {
        return getTicksPerMM(null, null);
    }

    public float getTicksPerMM(MOTOR_SIDE side) {
        return getTicksPerMM(side, null);
    }

    public float getTicksPerMM(MOTOR_SIDE side, MOTOR_END end) {
        if (!isAvailable()) {
            return 0.0f;
        }
        float ticks = 0.0f;
        Integer index = findEncoderIndex(side, end);
        if (index != null) {
            ticks = config.motors[index].ticksPerMM;
        }
        return ticks;
    }

    public int getEncoder() {
        return getEncoder(null, null);
    }

    public int getEncoder(MOTOR_SIDE side) {
        return getEncoder(side, null);
    }

    public int getEncoder(MOTOR_SIDE side, MOTOR_END end) {
        int position = 0;
        Integer index = findEncoderIndex(side, end);
        if (index != null) {
            position = getEncoder(index);
        }
        return position;
    }

    public float getRate() {
        return getRate(null, null);
    }

    public float getRate(MOTOR_SIDE side) {
        return getRate(side, null);
    }

    public float getRate(MOTOR_SIDE side, MOTOR_END end) {
        if (!isAvailable()) {
            return 0.0f;
        }

        // Update all PIDs
        for (int i = 0; i < config.motors.length; i++) {
            if (pids[i] != null) {
                pids[i].input(getEncoder(i));
            }
        }

        // Find something that matches the filter
        float rate = 0.0f;
        Integer index = findEncoderIndex(side, end);
        if (index != null) {
            rate = pids[index].last; // .last not .rate because we're using RatePID
        }
        return rate;
    }

    public void setSpeed(float speed) {
        for (MOTOR_SIDE side : MOTOR_SIDE.values()) {
            setSpeed(speed, side);
        }
    }

    public void setSpeed(float speed, MOTOR_SIDE side) {
        if (side == null) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": Null SIDE");
        }
        if (!isAvailable()) {
            return;
        }

        // Update the matching PIDs and run one of them
        speed = limit(speed);
        float pidSpeed = 0.0f;
        for (int i = 0; i < config.motors.length; i++) {
            if (config.motors[i].side == side && pids[i] != null) {
                // Special case to settle at exactly 0 instantly
                if (speed == 0.0f) {
                    pids[i].setTarget(speed);
                    pids[i].reset();
                    if (DEBUG) {
                        telemetry.log().add(side + "(" + Round.truncate(pidSpeed) + "): reset");
                    }
                } else {
                    pids[i].setTarget(speed * config.motors[i].maxRate * speedScale);
                    pidSpeed = pids[i].run(getEncoder(side));
                    // Motion is unreliable at very low speeds so set a minimum PID speed
                    // So long as SPEED_DEADZONE < JOYSTICK_DEADZONE this only affects auto
                    pidSpeed = Math.copySign(Math.max(SPEED_DEADZONE, Math.abs(pidSpeed)), pidSpeed);
                    if (DEBUG) {
                        telemetry.log().add(side + " (" + Round.truncate(pidSpeed) + "):\t" +
                                "t: " + Round.truncate(pids[i].target) + "\t\t" +
                                "l: " + Round.truncate(pids[i].last) + "\t" +
                                "e: " + Round.truncate(pids[i].error) + "\t" +
                                "m: " + Round.truncate(config.motors[i].maxRate) + "\t" +
                                "s: " + Round.truncate(speedScale)
                        );
                    }
                }
            }
        }
        setPowerRaw(pidSpeed, side);
    }

    public void setPowerRaw(float speed) {
        for (MOTOR_SIDE side : MOTOR_SIDE.values()) {
            setPowerRaw(speed, side);
        }
    }

    public void setPowerRaw(float speed, MOTOR_SIDE side) {
        if (!isAvailable()) {
            return;
        }
        for (WheelMotor motor : config.motors) {
            if (motor.side == side) {
                motor.motor.setPower(speed);
            }
        }
    }

    public void stop() {
        if (!isAvailable()) {
            return;
        }
        for (int i = 0; i < config.motors.length; i++) {
            config.motors[i].motor.setPower(0.0f);
            if (pids[i] != null) {
                pids[i].reset();
            }
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

    public void setSpeedScale(float scale) {
        this.speedScale = limit(scale);
    }

    public void loop(Gamepad pad) {
        if (!isAvailable() || !isTeleop() || pad == null) {
            return;
        }

        // Negative is forward; this is typically the opposite of native motor config
        float left = cleanJoystick(-pad.left_stick_y);
        this.setPowerRaw(left, MOTOR_SIDE.LEFT);

        float right = cleanJoystick(-pad.right_stick_y);
        this.setPowerRaw(right, MOTOR_SIDE.RIGHT);
    }

    protected float limit(float input) {
        return com.qualcomm.robotcore.util.Range.clip(input, -1.0f, 1.0f);
    }

    protected float cleanJoystick(float power) {
        power = limit(power);
        if (Math.abs(power) < JOYSTICK_DEADZONE) {
            power = 0.0f;
        }
        return power;
    }
}
