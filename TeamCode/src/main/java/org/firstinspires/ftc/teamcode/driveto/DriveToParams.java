package org.firstinspires.ftc.teamcode.driveto;

import org.firstinspires.ftc.teamcode.utils.Heading;

public class DriveToParams {
    private static final double ROTATION_TARGET_RANGE = Heading.QUARTER_CIRCLE;

    // Administrative members
    public final DriveToListener parent;
    public final Object reference;
    public int timeout = DriveTo.TIMEOUT_DEFAULT;
    public PID pid = new PID();
    public double pidAccumulatorMax = DriveTo.I_MAX_DEFAULT;

    // Comparison data
    public DriveToComp comparator = DriveToComp.LESS;
    public double limit = 0.0d;
    public double limitRange = 0.0d; // Used in range, rotational, and PID comparators
    public boolean crossing = false; // Used in rotational comparators

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

    public void pid(double target, double tolerance, PIDParams params) {
        this.comparator = DriveToComp.PID;
        this.pid = new PID(params);
        this.pid.setTarget(target);
        this.limit = target;
        this.limitRange = Math.abs(tolerance);
    }

    public void rotationPid(double target, double tolerance, PIDParams params) {
        this.comparator = DriveToComp.ROTATION_PID;
        this.pid = new PID(params);
        this.pid.setTarget(target);
        this.limit = Heading.normalize(target);
        this.limitRange = Math.abs(tolerance);
    }

    // And so on
    // Setters are optional (due to public members) but useful for readability
}