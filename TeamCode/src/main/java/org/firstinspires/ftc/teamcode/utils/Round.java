package org.firstinspires.ftc.teamcode.utils;

public class Round {
    public static double truncate(double val, int digits) {
        double factor = Math.pow(10, digits);
        return (double) ((int) (val * factor)) / factor;
    }

    public static double truncate(double val) {
        return truncate(val, 2);
    }

    public static float truncate(float val, int digits) {
        return (float) truncate((double) val, digits);
    }

    public static float truncate(float val) {
        return (float) truncate((double) val);
    }

    public static int magnitude(double val) {
        return (int) Math.ceil(Math.log10(val));
    }

    public static double magnitudeValue(double val) {
        return Math.pow(10, magnitude(val));
    }

    public static float magnitudeValue(float val) {
        return (float) magnitudeValue((double) val);
    }
}
