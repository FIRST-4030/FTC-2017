package org.firstinspires.ftc.teamcode.actuators;

import org.firstinspires.ftc.teamcode.config.Config;

public class MotorConfig implements Config {
    public final String name;
    public final boolean reverse;

    public MotorConfig(String name, boolean reverse) {
        this.name = name;
        this.reverse = reverse;
    }

    public MotorConfig(String name) {
        this(name, false);
    }
}
