package org.firstinspires.ftc.teamcode.actuators;

import org.firstinspires.ftc.teamcode.config.Config;

public class MotorConfig implements Config {
    public final String name;
    public final boolean reverse;
    public final boolean brake;

    public MotorConfig(String name, boolean reverse, boolean brake) {
        this.name = name;
        this.reverse = reverse;
        this.brake = brake;
    }

    public MotorConfig(String name, boolean reverse) {
        this(name, reverse, false);
    }

    public MotorConfig(String name) {
        this(name, false, false);
    }
}
