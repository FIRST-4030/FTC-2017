package org.firstinspires.ftc.teamcode.robot.config;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.robot.SWITCHES;
import org.firstinspires.ftc.teamcode.sensors.switches.Digital;
import org.firstinspires.ftc.teamcode.sensors.switches.SWITCH_TYPES;
import org.firstinspires.ftc.teamcode.sensors.switches.Switch;
import org.firstinspires.ftc.teamcode.sensors.switches.SwitchConfig;
import org.firstinspires.ftc.teamcode.sensors.switches.Voltage;
import org.firstinspires.ftc.teamcode.config.BOT;
import org.firstinspires.ftc.teamcode.config.Configs;

public class SwitchConfigs extends Configs {
    public SwitchConfigs(HardwareMap map, Telemetry telemetry, BOT bot) {
        super(map, telemetry, bot);
    }

    public Switch init(SWITCHES name) {
        SwitchConfig config = config(name);
        super.checkConfig(config, name);
        Switch button = null;
        switch (config.type) {
            case DIGITAL:
                button = new Digital(map, telemetry, config.name);
                break;
            case VOLTAGE:
                button = new Voltage(map, telemetry, config.name);
                break;
        }
        super.checkAvailable(button, name);
        return button;
    }

    public SwitchConfig config(SWITCHES name) {
        super.checkBOT();

        SwitchConfig config = null;
        switch (bot) {
            case WestCoast:
                switch (name) {
                    case LIFT:
                        config = new SwitchConfig(SWITCH_TYPES.VOLTAGE, "LS1");
                        break;
                }
                break;
            case Mecanum:
                switch (name) {
                    case LIFT:
                        config = new SwitchConfig(SWITCH_TYPES.VOLTAGE, "LS1");
                        break;
                }
                break;
        }
        return config;
    }
}
