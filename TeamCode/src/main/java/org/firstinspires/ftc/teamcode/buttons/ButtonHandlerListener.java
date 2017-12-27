package org.firstinspires.ftc.teamcode.buttons;

public interface ButtonHandlerListener {
    void onButtonHandler();

    double get(String name);

    double getDouble(String name);

    int getInt(String name);

    void set(String name, int value);

    void set(String name, double value);
}
