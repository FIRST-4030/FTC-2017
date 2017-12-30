package org.firstinspires.ftc.teamcode.actuators;

import org.firstinspires.ftc.teamcode.config.Config;

public class ServoConfig implements Config {
    public final String name;
    public final boolean reverse;
    public final float min;
    public final float max;

    public ServoConfig(String name, boolean reverse, float min, float max) {
        this.name = name;
        this.reverse = reverse;
        this.min = min;
        this.max = max;
    }

    public ServoConfig(String name) {
        this(name, false, 0.0f, 1.0f);
    }
}
