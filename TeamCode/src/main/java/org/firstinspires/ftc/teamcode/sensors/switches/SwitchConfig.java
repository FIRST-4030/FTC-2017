package org.firstinspires.ftc.teamcode.sensors.switches;

import org.firstinspires.ftc.teamcode.config.Config;

public class SwitchConfig implements Config {
    public final String name;
    public final SWITCH_TYPES type;

    public SwitchConfig(SWITCH_TYPES type, String name) {
        this.name = name;
        this.type = type;
    }
}
