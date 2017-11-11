package org.firstinspires.ftc.teamcode.config;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.actuators.ServoFTCConfig;

public class ServoConfigs {
    private static BOT bot = null;

    public ServoFTC init(HardwareMap map, Telemetry telemetry, String name) {
        ServoFTC servo = null;
        for (BOT i : BOT.values()) {
            bot = i;
            servo = new ServoFTC(map, config(name), telemetry);
            if (servo.isAvailable()) {
                if (bot.ordinal() != 0) {
                    telemetry.log().add("NOTICE: Using " + name + " servo config " + bot);
                }
                break;
            }
        }
        assert servo != null;
        if (!servo.isAvailable()) {
            telemetry.log().add("ERROR: Unable to initialize servo: " + name);
        }
        return servo;
    }

    private ServoFTCConfig config(String name) {
        ServoFTCConfig config = null;
        assert bot != null;
        switch (bot) {
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
        }
        return config;
    }
}
