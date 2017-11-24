package org.firstinspires.ftc.teamcode.config;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.utils.Available;

abstract public class Configs {
    protected HardwareMap map = null;
    protected Telemetry telemetry = null;
    protected BOT bot = null;

    protected Configs(HardwareMap map, Telemetry telemetry, BOT bot) throws IllegalArgumentException {
        if (map == null || telemetry == null || bot == null) {
            throw new IllegalArgumentException(this.getClass().getName() +
                    ": Null HardwareMap, Telemetry, or BOT");
        }
        this.map = map;
        this.telemetry = telemetry;
        this.bot = bot;
    }

    protected static void checkNull(Object obj, String name) throws IllegalArgumentException {
        if (obj == null) {
            throw new IllegalArgumentException("Null " + name);
        }
    }

    protected void checkBOT() throws IllegalArgumentException {
        checkNull(bot, BOT.class.getName());
    }

    protected void checkConfig(Config config) throws IllegalArgumentException {
        checkConfig(config, null);
    }

    protected void checkConfig(Config config, Object name) throws IllegalArgumentException {
        if (config == null) {
            String error = this.getClass().getName() + ": Not configured: " + bot;
            if (name != null) {
                error += ": " + name;
            }
            throw new IllegalArgumentException(error);
        }
    }

    protected void checkAvailable(Available device) {
        checkAvailable(device, null);
    }

    protected void checkAvailable(Available device, Object name) {
        if (device == null || !device.isAvailable()) {
            String error = this.getClass().getName() + ": Unable to initialize: " + bot;
            if (name != null) {
                error += ": " + name;
            }
            System.err.println(error);
            telemetry.log().add(error);
        }
    }
}
