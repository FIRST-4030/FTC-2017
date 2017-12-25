package org.firstinspires.ftc.teamcode.driveto;

public enum DriveToComp {
    LESS(false),
    GREATER(false),
    IN_RANGE(false),
    OUTSIDE_RANGE(false),
    ROTATION_LESS(true),
    ROTATION_GREATER(true),
    PID(true),
    ROTATION_PID(true);

    private final boolean rotational;

    DriveToComp(boolean rotational) {
        this.rotational = rotational;
    }

    public boolean rotataional() {
        return rotational;
    }
}
