package org.firstinspires.ftc.teamcode.config;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.actuators.MotorConfig;

public class MotorConfigs {
    private HardwareMap map = null;
    private Telemetry telemetry = null;
    private BOT bot = null;

    public MotorConfigs(HardwareMap map, Telemetry telemetry, BOT bot) {
        this.map = map;
        this.telemetry = telemetry;
        this.bot = bot;
    }

    public MotorConfigs(HardwareMap map, Telemetry telemetry) {
        this(map, telemetry, null);
    }

    public Motor init(String name) {
        Motor motor = null;
        if (bot != null) {
            motor = new Motor(map, config(name, bot), telemetry);
        } else {
            for (BOT b : BOT.values()) {
                motor = new Motor(map, config(name, b), telemetry);
                if (motor.isAvailable()) {
                    bot = b;
                    if (bot.ordinal() != 0) {
                        telemetry.log().add("NOTICE: Using " + name + " motor config " + bot);
                    }
                    break;
                }
            }
        }
        assert motor != null;
        if (!motor.isAvailable()) {
            telemetry.log().add("ERROR: Unable to initialize motor: " + name);
        }
        return motor;
    }

    private static MotorConfig config(String name, BOT b) {
        MotorConfig config = null;
        assert b != null;
        switch (b) {
            case FINAL:
                config = FinalBot(name);
                break;
            case CODE:
                config = CodeBot(name);
                break;
        }
        return config;
    }

    private static MotorConfig CodeBot(String name) {
        MotorConfig config = null;
        switch (name) {
            default:
                break;
        }
        return config;
    }

    private static MotorConfig FinalBot(String name) {
        MotorConfig config = null;
        switch (name) {
            case "LIFT":
                config = new MotorConfig("LM1", false);
                break;
            case "lBumperM":
                config = new MotorConfig("lBumperM", false);
                break;
            case "rBumperM":
                config = new MotorConfig("rBumperM", false);
                break;
        }
        return config;
    }
}
