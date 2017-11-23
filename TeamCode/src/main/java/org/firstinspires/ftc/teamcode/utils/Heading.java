package org.firstinspires.ftc.teamcode.utils;

public class Heading {

    public static final int FULL_CIRCLE = 360;

    /**
     * @param heading Any heading
     * @return The same heading projected into the space between 0 and 359, inclusively
     */
    public static int normalize(int heading) {
        return ((heading % FULL_CIRCLE) + FULL_CIRCLE) % FULL_CIRCLE;
    }
}
