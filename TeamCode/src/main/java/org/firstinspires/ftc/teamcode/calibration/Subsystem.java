package org.firstinspires.ftc.teamcode.calibration;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.robot.Robot;

abstract public class Subsystem {
    protected final OpMode opmode;
    protected final Robot robot;
    protected final ButtonHandler buttons;
    private boolean active;

    public abstract String name();

    protected abstract void load();

    protected abstract void unload();

    protected abstract void update();

    public Subsystem(OpMode opmode, Robot robot, ButtonHandler buttons) {
        this.opmode = opmode;
        this.robot = robot;
        this.buttons = buttons;
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }

    public void activate() {
        setActive(true);
    }

    public void deactivate() {
        setActive(false);
    }

    private void setActive(boolean active) {
        // Short-circuit if there is no change
        if (active == isActive()) {
            return;
        }

        // Load or unload
        if (active) {
            load();
        } else {
            unload();
        }
        this.active = active;
    }

    public void loop() {
        if (!isActive()) {
            return;
        }
        update();
    }

    public void toggle() {
        setActive(!isActive());
    }
}
