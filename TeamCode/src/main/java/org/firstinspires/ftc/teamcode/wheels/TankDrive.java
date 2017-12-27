package org.firstinspires.ftc.teamcode.wheels;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.driveto.RatePID;
import org.firstinspires.ftc.teamcode.utils.Round;

public class TankDrive implements Wheels {
    private static final boolean DEBUG = false;

    protected WheelsConfig config = null;
    protected final Telemetry telemetry;
    protected double speedScale = 1.0d;
    private boolean teleop = false;
    private int[] offsets;
    private RatePID[] pids;

    public TankDrive(HardwareMap map, Telemetry telemetry, WheelsConfig config) {
        this.telemetry = telemetry;
        for (WheelMotor motor : config.motors) {
            if (motor == null) {
                telemetry.log().add(this.getClass().getSimpleName() + ": Null motor");
                break;
            }
            if (motor.name == null || motor.name.isEmpty()) {
                throw new IllegalArgumentException(this.getClass().getSimpleName() + ": Null motor or null/empty motor name");
            }
            try {
                motor.motor = map.dcMotor.get(motor.name);
                if (motor.reverse) {
                    motor.motor.setDirection(DcMotorSimple.Direction.REVERSE);
                }
            } catch (Exception e) {
                telemetry.log().add(this.getClass().getSimpleName() + ": No such device: " + motor.name);
                return;
            }
        }
        this.offsets = new int[config.motors.length];
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

    public double getTicksPerMM() {
        return getTicksPerMM(null, null);
    }

    public double getTicksPerMM(MOTOR_SIDE side) {
        return getTicksPerMM(side, null);
    }

    public double getTicksPerMM(MOTOR_SIDE side, MOTOR_END end) {
        if (!isAvailable()) {
            return 0;
        }
        double ticks = 0.0d;
        Integer index = findEncoderIndex(side, end);
        if (index != null) {
            ticks = config.motors[index].pid.ticksPerMM;
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

    public int getEncoder(int index) {
        if (!isAvailable()) {
            return 0;
        }
        if (index < 0 || index >= config.motors.length) {
            throw new ArrayIndexOutOfBoundsException(this.getClass().getSimpleName() + ": Invalid index: " + index);
        }
        if (!config.motors[index].encoder) {
            telemetry.log().add("No encoder on motor: " + index);
            return 0;
        }
        return (int) ((double) (config.motors[index].motor.getCurrentPosition() + offsets[index]));
    }

    public double getRate() {
        return getRate(null, null);
    }

    public double getRate(MOTOR_SIDE side) {
        return getRate(side, null);
    }

    public double getRate(MOTOR_SIDE side, MOTOR_END end) {
        if (!isAvailable()) {
            return 0.0d;
        }

        // Update all PIDs
        for (int i = 0; i < config.motors.length; i++) {
            if (pids[i] != null) {
                pids[i].input(getEncoder(i));
            }
        }

        // Find something that matches the filter
        double rate = 0.0d;
        Integer index = findEncoderIndex(side, end);
        if (index != null) {
            rate = pids[index].last; // .last not .rate because we're using RatePID
        }
        return rate;
    }

    public void setSpeed(double speed) {
        for (MOTOR_SIDE side : MOTOR_SIDE.values()) {
            setSpeed(speed, side);
        }
    }

    public void setSpeed(double speed, MOTOR_SIDE side) {
        if (side == null) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": Null SIDE");
        }
        if (!isAvailable()) {
            return;
        }

        // Update the matching PIDs and run one of them
        speed = limit(speed);
        double pidSpeed = 0.0d;
        for (int i = 0; i < config.motors.length; i++) {
            if (config.motors[i].side == side && pids[i] != null) {
                // Special case to settle at exactly 0 instantly
                if (speed == 0.0d) {
                    pids[i].setTarget(speed);
                    pids[i].reset();
                    if (DEBUG) {
                        telemetry.log().add(side + "(" + Round.truncate(pidSpeed) + "): reset");
                    }
                } else {
                    pids[i].setTarget(speed * config.motors[i].pid.maxRate * speedScale);
                    pidSpeed = pids[i].run(getEncoder(side));
                    if (DEBUG) {
                        telemetry.log().add(side + " (" + Round.truncate(pidSpeed) + "):\t" +
                                "t: " + Round.truncate(pids[i].target) + "\t\t" +
                                "l: " + Round.truncate(pids[i].last) + "\t" +
                                "e: " + Round.truncate(pids[i].error)
                        );
                    }
                }
            }
        }
        setPowerRaw(pidSpeed, side);
    }

    public void setPowerRaw(double speed) {
        for (MOTOR_SIDE side : MOTOR_SIDE.values()) {
            setPowerRaw(speed, side);
        }
    }

    public void setPowerRaw(double speed, MOTOR_SIDE side) {
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
            config.motors[i].motor.setPower(0.0d);
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

    public void setSpeedScale(double scale) {
        this.speedScale = limit(scale);
    }

    public void loop(Gamepad pad) {
        if (!isAvailable() || !isTeleop() || pad == null) {
            return;
        }

        // Negative is forward; this is typically the opposite of native motor config
        double left = cleanJoystick(-pad.left_stick_y);
        this.setSpeed(left, MOTOR_SIDE.LEFT);

        double right = cleanJoystick(-pad.right_stick_y);
        this.setSpeed(right, MOTOR_SIDE.RIGHT);
    }

    protected double limit(double input) {
        return com.qualcomm.robotcore.util.Range.clip(input, -1.0d, 1.0d);
    }

    protected double cleanJoystick(double power) {
        power = limit(power);
        if (power < 0.1 && power > -0.1) {
            return 0;
        } else {
            return power;
        }
    }
}
