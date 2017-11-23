package org.firstinspires.ftc.teamcode.robot.configs;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

abstract public class Configs {
    protected HardwareMap map = null;
    protected Telemetry telemetry = null;
    protected BOT bot = null;

    public Configs(HardwareMap map, Telemetry telemetry, BOT bot) {
        if (map == null || telemetry == null || bot == null) {
            throw new IllegalArgumentException("Null HardwareMap, Telemetry, or BOT");
        }
        this.map = map;
        this.telemetry = telemetry;
        this.bot = bot;
    }
}
