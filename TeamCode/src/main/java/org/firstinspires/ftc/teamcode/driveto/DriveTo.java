package org.firstinspires.ftc.teamcode.driveto;

import org.firstinspires.ftc.teamcode.utils.Heading;

public class DriveTo {
    public static final int TIMEOUT_DEFAULT = 3000;

    // Run-time
    public final boolean any;
    private boolean done;
    private long started = 0;
    private final DriveToParams[] params;

    public DriveTo(DriveToParams[] params, boolean any) {
        this.any = any;
        this.done = false;
        this.started = 0;
        this.params = params;
    }

    public DriveTo(DriveToParams[] params) {
        this(params, false);
    }

    public boolean isDone() {
        return done;
    }

    public boolean isStarted() {
        return (started > 0);
    }

    public boolean isTimeout() {
        long now = System.currentTimeMillis();
        for (DriveToParams param : params) {
            if (now - started > param.timeout) {
                return true;
            }
        }
        return false;
    }

    public void drive() {
        if (!isStarted()) {
            this.started = System.currentTimeMillis();
        }

        boolean stop = false;
        if (onTarget() || isTimeout()) {
            stop = true;
        }

        if (stop) {
            for (DriveToParams param : params) {
                param.parent.driveToStop(param);
            }
            done = true;
        } else {
            done = false;
            for (DriveToParams param : params) {
                param.parent.driveToRun(param);
            }
        }
    }

    public boolean onTarget() {
        for (DriveToParams param : params) {
            boolean onTarget = false;
            float actual = param.parent.driveToSensor(param);
            float heading = 0.0f;

            // Special handling for rotational contexts and PIDs
            if (param.comparator.rotational()) {
                heading = Heading.normalize(actual);
                if (param.comparator.pid()) {
                    param.pid.inputRotational(actual);
                }
            } else {
                if (param.comparator.pid()) {
                    param.pid.input(actual);
                }
            }

            switch (param.comparator) {
                case LESS:
                    if (actual <= param.limit) {
                        onTarget = true;
                    }
                    break;
                case GREATER:
                    if (actual >= param.limit) {
                        onTarget = true;
                    }
                    break;
                case IN_RANGE:
                    if (actual >= param.limit && actual <= param.limitRange) {
                        onTarget = true;
                    }
                    break;
                case OUTSIDE_RANGE:
                    if (actual < param.limit || actual > param.limitRange) {
                        onTarget = true;
                    }
                    break;
                case ROTATION_LESS:
                    if (!param.crossing) {
                        if ((heading <= param.limit) && (heading > param.limitRange)) {
                            onTarget = true;
                        }
                    } else {
                        if ((heading <= param.limit) || (heading > param.limitRange)) {
                            onTarget = true;
                        }
                    }
                    break;
                case ROTATION_GREATER:
                    if (!param.crossing) {
                        if ((heading >= param.limit) && (heading < param.limitRange)) {
                            onTarget = true;
                        }
                    } else {
                        if ((heading >= param.limit) || (heading < param.limitRange)) {
                            onTarget = true;
                        }
                    }
                    break;
                case PID:
                case ROTATION_PID:
                    if (Math.abs(param.pid.error) < param.limitRange &&
                            (param.diffRange == null ||
                                    Math.abs(param.pid.differential) < param.diffRange)) {
                        onTarget = true;
                    }
                    break;
            }

            // In ANY mode any match will do
            // In ALL mode any failure will do
            if (onTarget && any) {
                return true;
            } else if (!onTarget) {
                return false;
            }
        }

        // We only get here in ANY mode if there are no matches or in ALL mode when there are no failures
        return !any;
    }
}