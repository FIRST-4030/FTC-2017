package org.firstinspires.ftc.teamcode.robot.configs;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.actuators.ServoConfig;
import org.firstinspires.ftc.teamcode.robot.SERVOS;

public class ServoConfigs {
    private HardwareMap map = null;
    private Telemetry telemetry = null;
    private BOT bot = null;

    public ServoConfigs(HardwareMap map, Telemetry telemetry, BOT bot) {
        if (map == null || bot == null) {
            throw new IllegalArgumentException("Null HardwareMap or BOT");
        }
        this.map = map;
        this.telemetry = telemetry;
        this.bot = bot;
    }

    public ServoFTC init(SERVOS name) {
        ServoConfig config = config(name, bot);
        if (config == null) {
            throw new IllegalArgumentException("No servo configured for: " + bot + ":" + name);
        }
        ServoFTC servo = new ServoFTC(map, telemetry, config);
        if (!servo.isAvailable()) {
            telemetry.log().add("Unable to initialize servo: " + bot + ":" + name);
        }
        return servo;
    }

    public static ServoConfig config(SERVOS servo, BOT bot) {
        ServoConfig config = null;
        switch (bot) {
            case WestCoast:
                switch (servo) {
                    case CLAW_TOP:
                        config = new ServoConfig("CL1", true, 0.67d, 0.96d);
                        break;
                    case CLAW_BOTTOM:
                        config = new ServoConfig("CL2", true, 0.11d, 0.41d);
                        break;
                    case INTAKE_LEFT:
                        config = new ServoConfig("lBumperS", true, 0.44d, 0.61d);
                        break;
                    case INTAKE_RIGHT:
                        config = new ServoConfig("rBumperS", true, 0.59d, 0.83d);
                        break;
                }
                break;
            case Mecanum:
                switch (servo) {
                    case CLAW_TOP:
                        break;
                    case CLAW_BOTTOM:
                        break;
                    case INTAKE_LEFT:
                        break;
                    case INTAKE_RIGHT:
                        break;
                }
                break;
        }
        return config;
    }
}
