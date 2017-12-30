package org.firstinspires.ftc.teamcode.driveto;

import org.firstinspires.ftc.teamcode.utils.Heading;

public class DriveToParams {
    private static final float ROTATION_TARGET_RANGE = Heading.QUARTER_CIRCLE;

    // Administrative members
    public final DriveToListener parent;
    public final Object reference;
    public int timeout = DriveTo.TIMEOUT_DEFAULT;
    public PID pid = new PID();

    // Comparison data
    public DriveToComp comparator = DriveToComp.LESS;
    public float limit = 0.0f;
    public float limitRange = 0.0f; // Used in range, rotational, and PID comparators
    public Float diffRange = null; // Used in PID comparators
    public boolean crossing = false; // Used in rotational comparators

    public DriveToParams(DriveToListener parent, Object reference) {
        this.parent = parent;
        this.reference = reference;
    }

    public void lessThan(float limit) {
        this.comparator = DriveToComp.LESS;
        this.limit = limit;
    }

    public void lessThan(int limit) {
        lessThan((float) limit);
    }

    public void greaterThan(float limit) {
        this.comparator = DriveToComp.GREATER;
        this.limit = limit;
    }

    public void greaterThan(int limit) {
        greaterThan((float) limit);
    }

    public void rotationLess(float limit) {
        this.comparator = DriveToComp.ROTATION_LESS;
        this.limit = Heading.normalize(limit);
        this.limitRange = Heading.normalize(limit - ROTATION_TARGET_RANGE);
        this.crossing = this.limitRange > this.limit;
    }

    public void rotationGreater(float limit) {
        this.comparator = DriveToComp.ROTATION_GREATER;
        this.limit = Heading.normalize(limit);
        this.limitRange = Heading.normalize(limit + ROTATION_TARGET_RANGE);
        this.crossing = this.limitRange < this.limit;
    }

    public void pid(float target, PIDParams params, float tolerance, Float diffTolerance) {
        this.comparator = DriveToComp.PID;
        this.pid = new PID(params);
        this.pid.setTarget(target);
        this.limit = target;
        this.limitRange = Math.abs(tolerance);
        if (diffTolerance != null) {
            this.diffRange = Math.abs(diffTolerance);
        }
    }

    public void rotationPid(float target, PIDParams params, float tolerance, Float diffTolerance) {
        this.comparator = DriveToComp.ROTATION_PID;
        this.pid = new PID(params);
        this.pid.setTarget(target);
        this.limitRange = Math.abs(tolerance);
        if (diffTolerance != null) {
            this.diffRange = Math.abs(diffTolerance);
        }
    }

    // And so on
    // Setters are optional (due to public members) but useful for readability
}