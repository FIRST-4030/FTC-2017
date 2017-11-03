package org.firstinspires.ftc.teamcode.utils;

public class OrderedEnumHelper {
    public static <E extends Enum<E> & OrderedEnum> E prev(E e) {
        Enum<E>[] values = e.getClass().getEnumConstants();
        int i = e.ordinal() - 1;
        if (i < 0) {
            i = 0;
        }
        return (E)(values[i]);
    }

    public static <E extends Enum<E> & OrderedEnum> E next(E e) {
        Enum<E>[] values = e.getClass().getEnumConstants();
        int i = e.ordinal() + 1;
        if (i >= values.length) {
            i = values.length - 1;
        }
        return (E)(values[i]);
    }
}
