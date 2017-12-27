package org.firstinspires.ftc.teamcode.buttons;

public interface ButtonType {
    void update(boolean pressed);

    boolean active();

    boolean held();
}
