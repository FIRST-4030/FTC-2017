package org.firstinspires.ftc.teamcode.calibration;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.robot.Robot;

abstract public class Subsystem {
    protected final OpMode opmode;
    protected final Robot robot;
    protected final ButtonHandler buttons;

    public Subsystem(OpMode opmode, Robot robot, ButtonHandler buttons) {
        this.opmode = opmode;
        this.robot = robot;
        this.buttons = buttons;
    }

    public abstract String name();

    public abstract void load();

    public abstract void unload();

    public abstract void loop();
}
