package org.firstinspires.ftc.teamcode.robot.lift;

import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.backgroundTask.Background;
import org.firstinspires.ftc.teamcode.driveto.PID;
import org.firstinspires.ftc.teamcode.driveto.PIDParams;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.sensors.switches.Switch;

/**
 * Created by Black Shadow on 4/20/2018.
 */

public class Lift extends Background {

    public static final int LOW = 0;
    public static final int MIDDLE = 5000;
    public static final int HIGH = 10000;

    private boolean initialized = false;
    private PID pid = new PID(new PIDParams());
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

            motor.setPower(.25f);

        } else {
            pid.input(motor.getEncoder());
//            motor.setPower(pid.output());
            Robot.robot.telemetry.log().add("PID output: " + pid.output());
        }
    }


    public void set(int target) {
        if (!isAvailable()) return;
        pid.setTarget(target);
    }

    public boolean isAvailable() {
        return initialized;
    }

}
