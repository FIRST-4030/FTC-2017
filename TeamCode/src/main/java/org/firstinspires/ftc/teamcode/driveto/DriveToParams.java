package org.firstinspires.ftc.teamcode.driveto;

import org.firstinspires.ftc.teamcode.utils.Heading;

public class DriveToParams {
    private static final double ROTATION_TARGET_RANGE = Heading.QUARTER_CIRCLE;

    // Administrative members
    public final DriveToListener parent;
    public final Object reference;
    public int timeout = DriveTo.TIMEOUT_DEFAULT;

    // Comparison data
    public DriveToComp comparator = DriveToComp.LESS;
    public double limit = 0.0d;
    public double limitRange = 0.0d; // Used in range and rotational comparators
    public boolean crossing = false; // Used in rotational comparators

    // PID values
    public long timestamp = 0;
    public double error = 0.0d;
    public double accumulated = 0.0d;
    public double differential = 0.0d;

    public DriveToParams(DriveToListener parent, Object reference) {
        this.parent = parent;
        this.reference = reference;
    }

    public void lessThan(double limit) {
        this.comparator = DriveToComp.LESS;
        this.limit = limit;
    }

    public void lessThan(int limit) {
        lessThan((double) limit);
    }

    public void greaterThan(double limit) {
        this.comparator = DriveToComp.GREATER;
        this.limit = limit;
    }

    public void greaterThan(int limit) {
        greaterThan((double) limit);
    }

    public void rotationLess(double limit) {
        this.comparator = DriveToComp.ROTATION_LESS;
        this.limit = Heading.normalize(limit);
        this.limitRange = Heading.normalize(limit - ROTATION_TARGET_RANGE);
        this.crossing = this.limitRange > this.limit;
    }

    public void rotationGreater(double limit) {
        this.comparator = DriveToComp.ROTATION_GREATER;
        this.limit = Heading.normalize(limit);
        this.limitRange = Heading.normalize(limit + ROTATION_TARGET_RANGE);
        this.crossing = this.limitRange < this.limit;
    }

    // And so on
    // Setters/getters are optional since the members are public but might make use easier
}