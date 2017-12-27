package org.firstinspires.ftc.teamcode.utils;

public class Round {
    public static double truncate(double val, int digits) {
        double factor = Math.pow(10, digits);
        return (double) ((int) (val * factor)) / factor;
    }

    public static double truncate(double val) {
        return truncate(val, 2);
    }
}
