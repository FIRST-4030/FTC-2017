package org.firstinspires.ftc.teamcode.buttons;

import com.qualcomm.robotcore.hardware.Gamepad;

public interface ButtonHandlerListener {
    void update(boolean pressed);
    boolean active();
}
