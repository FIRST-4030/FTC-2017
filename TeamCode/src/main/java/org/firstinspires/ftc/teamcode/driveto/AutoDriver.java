package org.firstinspires.ftc.teamcode.driveto;

public class AutoDriver {
    public boolean done = false;
    public float interval = 0;
    private float timer = 0;
    public DriveTo drive = null;
    public boolean timeout = false;

    public boolean isDone() {
        return done;
    }

    public boolean isRunning(double time) {
        if (interval > 0) {
            timer = (float) time + interval;
        }
        interval = 0;
        return (drive != null || timer > time);
    }
}
