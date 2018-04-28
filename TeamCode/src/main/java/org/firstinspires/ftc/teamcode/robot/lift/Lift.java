package org.firstinspires.ftc.teamcode.robot.lift;

import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.backgroundTask.Background;
import org.firstinspires.ftc.teamcode.driveto.PID;
import org.firstinspires.ftc.teamcode.driveto.PIDParams;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.sensors.switches.Switch;

public class Lift extends Background {

    public static final boolean DEBUG = false;

    public static final int MAX = 5750;
    public static final int HIGH = 5500;
    public static final int MIDDLE = HIGH / 2;
    public static final int LOW = 50;

    public static final float SPEED_CALIBRATION = 0.25f;

    PIDParams params = new PIDParams(0.008f, 0.0015f, 0.0f,
            10.0f, true, true);

    private boolean initialized = false;
    private PID pid = new PID(params);
    private Motor motor;
    private Switch button;

    public void init(Motor motor, Switch button) {
        this.motor = motor;
        this.button = button;
        pid.setTarget(LOW);
    }

    @Override
    protected void loop() {
        if (!isAvailable()) {
            if (!button.get()) {
                initialized = true;
                motor.resetEncoder();
                return;
            }
            motor.setPower(SPEED_CALIBRATION);
        } else {
            pid.input(motor.getEncoder());
            motor.setPower(pid.output());
            if (DEBUG) {
                Robot.robot.telemetry.log().add("A/T/P: " + motor.getEncoder() + "\t" + pid.target + "\t" + pid.output());
            }
        }
    }

    public void set(int target) {
        if (!isAvailable()) return;
        target = Math.max(target, LOW);
        target = Math.min(target, MAX);
        pid.setTarget(target);
    }

    public boolean isAvailable() {
        return initialized;
    }
}
