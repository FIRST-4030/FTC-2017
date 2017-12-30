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
     * Modulo arithmetic is not available for floats
     *
     * @param heading Any heading
     * @return The same heading projected into the space between 0 and 359, inclusively
     */
    public static float normalize(float heading) {
        while (heading >= (float) FULL_CIRCLE) {
            heading -= (float) FULL_CIRCLE;
        }
        while (heading < 0.0f) {
            heading += (float) FULL_CIRCLE;
        }
        return heading;
    }

    /**
     * Modulo arithmetic is not available for floats
     *
     * @param err Any heading differential
     * @return The same heading differential, projected into -180 to +180
     */
    public static float normalizeErr(float err) {
        if (err > HALF_CIRCLE) {
            err -= FULL_CIRCLE;
        } else if (err < -HALF_CIRCLE) {
            err += FULL_CIRCLE;
        }
        return err;
    }
}
