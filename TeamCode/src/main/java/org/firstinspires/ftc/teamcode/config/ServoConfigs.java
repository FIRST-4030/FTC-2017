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
        if (servo == null) {
            throw new IllegalArgumentException("No servo object available");
        }
        if (!servo.isAvailable()) {
            telemetry.log().add("ERROR: Unable to initialize servo: " + name);
        }
        return servo;
    }

    private static ServoFTCConfig config(String name, BOT b) {
        ServoFTCConfig config = null;
        if (b == null) {
            throw new IllegalArgumentException("Null BOT");
        }
        switch (b) {
            case FINAL:
                config = FinalBot(name, false);
                break;
            case CALIBRATION:
                config = FinalBot(name, true);
                break;
            case CODE:
                config = CodeBot(name);
                break;
        }
        return config;
    }

    private static double calMin(boolean calibrate, double limit) {
        return calibrateOrFinal(calibrate, true, limit);
    }

    private static double calMax(boolean calibrate, double limit) {
        return calibrateOrFinal(calibrate, false, limit);
    }

    private static double calibrateOrFinal(boolean calibrate, boolean min, double limit) {
        double calLimit = 1.0d;
        if (min) {
            calLimit = 0.0d;
        }
        if (calibrate) {
            return calLimit;
        }
        return limit;
    }

    private static ServoFTCConfig CodeBot(String name) {
        ServoFTCConfig config = null;
        switch (name) {
            default:
                break;
        }
        return config;
    }

    private static ServoFTCConfig FinalBot(String name, boolean cal) {
        ServoFTCConfig config = null;
        switch (name) {
            case "CLAW-TOP":
                config = new ServoFTCConfig("CL1", true, calMin(cal, 0.67d), calMax(cal, 0.96d));
                break;
            case "CLAW-BOTTOM":
                config = new ServoFTCConfig("CL2", true, calMin(cal, 0.11d), calMax(cal, 0.41d));
                break;
            case "LEFT-INTAKE":
                config = new ServoFTCConfig("lBumperS", true, calMin(cal, 0.44d), calMax(cal, 0.61d));
                break;
            case "RIGHT-INTAKE":
                config = new ServoFTCConfig("rBumperS", true, calMin(cal, 0.59d), calMax(cal, 0.83d));
                break;
        }
        return config;
    }
}
