package org.firstinspires.ftc.teamcode.utils;

public class Heading {

    public static final int FULL_CIRCLE = 360;
    public static final int HALF_CIRCLE = FULL_CIRCLE / 2;
    public static final int QUARTER_CIRCLE = FULL_CIRCLE / 4;

    /**
     * @param heading Any heading
     * @return The same heading projected into the space between 0 and 359, inclusively
     */
    public static int normalize(int heading) {
        return ((heading % FULL_CIRCLE) + FULL_CIRCLE) % FULL_CIRCLE;
    }

    /**
     * Modulo arithmetic is not available for doubles
     *
     * @param heading Any heading
     * @return The same heading projected into the space between 0 and 359, inclusively
     */
    public static double normalize(double heading) {
        while (heading >= (double) FULL_CIRCLE) {
            heading -= (double) FULL_CIRCLE;
        }
        while (heading < 0.0d) {
            heading += (double) FULL_CIRCLE;
        }
        return heading;
    }
}
