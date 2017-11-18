package org.firstinspires.ftc.teamcode.config;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.actuators.ServoFTCConfig;

public class ServoConfigs {
    private HardwareMap map = null;
    private Telemetry telemetry = null;
    private BOT bot = null;

    public ServoConfigs(HardwareMap map, Telemetry telemetry, BOT bot) {
        this.map = map;
        this.telemetry = telemetry;
        this.bot = bot;
    }

    public ServoConfigs(HardwareMap map, Telemetry telemetry) {
        this(map, telemetry, null);
    }

    public ServoFTC init(String name) {
        ServoFTC servo = null;
        if (bot != null) {
            servo = new ServoFTC(map, config(name, bot), telemetry);
        } else {
            for (BOT b : BOT.values()) {
                servo = new ServoFTC(map, config(name, b), telemetry);
                if (servo.isAvailable()) {
                    bot = b;
                    if (bot.ordinal() != 0) {
                        telemetry.log().add("NOTICE: Using " + name + " servo config " + bot);
                    }
                    break;
                }
            }
        }
        assert servo != null;
        if (!servo.isAvailable()) {
            telemetry.log().add("ERROR: Unable to initialize servo: " + name);
        }
        return servo;
    }

    private static ServoFTCConfig config(String name, BOT b) {
        ServoFTCConfig config = null;
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

    private static ServoFTCConfig CodeBot(String name) {
        ServoFTCConfig config = null;
        switch (name) {
            default:
                break;
        }
        return config;
    }

    private static ServoFTCConfig FinalBot(String name) {
        ServoFTCConfig config = null;
        switch (name) {
            case "CLAW-TOP":
                config = new ServoFTCConfig("CL1", true, 0.0, 0.48);
                break;
            case "CLAW-BOTTOM":
                config = new ServoFTCConfig("CL2", false, 0.09, 0.35);
                break;
            case "LEFT-INTAKE":
                config = new ServoFTCConfig("lBumperS", false, .3, .7);
                break;
            case "RIGHT_INTAKE":
                config = new ServoFTCConfig("rBumperS", false, .3, .7);
                break;
        }
        return config;
    }
}
