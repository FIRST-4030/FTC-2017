package org.firstinspires.ftc.teamcode.auto;

import org.firstinspires.ftc.teamcode.driveto.DriveTo;

public class AutoTracker {
    public boolean done = false;
    public double interval = 0;
    public double timer = 0;
    public DriveTo drive = null;

    public void setTimer(double time) {
        if (interval > 0) {
            timer = time + interval;
        }
        interval = 0;
    }
}
