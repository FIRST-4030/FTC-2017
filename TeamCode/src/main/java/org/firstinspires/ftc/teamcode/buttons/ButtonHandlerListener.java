package org.firstinspires.ftc.teamcode.buttons;

import com.qualcomm.robotcore.hardware.Gamepad;

public interface ButtonHandlerListener {
    public void update(boolean pressed);
    public boolean active();
}
