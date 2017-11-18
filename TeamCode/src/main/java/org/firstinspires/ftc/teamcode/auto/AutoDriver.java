package org.firstinspires.ftc.teamcode.auto;

import org.firstinspires.ftc.teamcode.driveto.DriveTo;

public class AutoDriver {
    public boolean done = false;
    public double interval = 0;
    public double timer = 0;
    public DriveTo drive = null;

    public boolean isDone() {
        return done;
    }

    public void setTime(double time) {
        isRunning(time);
    }

    public boolean isRunning(double time) {
        if (interval > 0) {
            timer = time + interval;
        }
        interval = 0;
        return (drive != null || timer > time);
    }
}
