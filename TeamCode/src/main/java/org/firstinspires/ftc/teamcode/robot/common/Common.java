package org.firstinspires.ftc.teamcode.robot.common;

import org.firstinspires.ftc.teamcode.robot.Robot;

/*
 * These are robot-specific helper methods
 * They exist to encourage code re-use across classes
 *
 * They are a reasonable template for future robots, but are unlikely to work as-is
 */
public class Common {

    // Jewel arm post-start retracted position
    public static final double JEWEL_ARM_RETRACT = 0.40d;

    // Runtime
    public final Lift lift;
    public final Jewel jewel;
    public final Drive drive;
    private final Robot robot;

    public Common(Robot r) {
        if (r == null) {
            throw new IllegalArgumentException(this.getClass().getName() + ": Null robot");
        }
        this.robot = r;

        this.lift = new Lift(robot);
        this.jewel = new Jewel(robot, this);
        this.drive = new Drive(robot);
    }
}
