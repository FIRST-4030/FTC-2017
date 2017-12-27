package org.firstinspires.ftc.teamcode.driveto;

public enum DriveToComp {
    LESS(false, false),
    GREATER(false, false),
    IN_RANGE(false, false),
    OUTSIDE_RANGE(false, false),
    ROTATION_LESS(true, false),
    ROTATION_GREATER(true, false),
    PID(true, true),
    ROTATION_PID(true, true);

    private final boolean rotational, pid;

    DriveToComp(boolean rotational, boolean pid) {
        this.rotational = rotational;
        this.pid = pid;
    }

    public boolean rotational() {
        return rotational;
    }

    public boolean pid() {
        return pid;
    }
}
