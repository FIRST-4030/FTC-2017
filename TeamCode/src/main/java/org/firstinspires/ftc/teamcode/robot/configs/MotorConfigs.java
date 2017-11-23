package org.firstinspires.ftc.teamcode.robot.configs;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.actuators.MotorConfig;
import org.firstinspires.ftc.teamcode.robot.MOTORS;

public class MotorConfigs extends Configs {
    public MotorConfigs(HardwareMap map, Telemetry telemetry, BOT bot) {
        super(map, telemetry, bot);

    }

    public Motor init(MOTORS name) {
        MotorConfig config = config(name, bot);
        if (config == null) {
            throw new IllegalArgumentException(this.getClass().getName() + ": Not configured: " + bot + ":" + name);
        }
        Motor motor = new Motor(map, telemetry, config);
        if (!motor.isAvailable()) {
            telemetry.log().add(this.getClass().getName() + ": Unable to initialize: " + bot + ":" + name);
        }
        return motor;
    }

    public static MotorConfig config(MOTORS motor, BOT bot) {
        MotorConfig config = null;
        if (motor == null) {
            throw new IllegalArgumentException("Null motor");
        }
        if (bot == null) {
            throw new IllegalArgumentException("Null BOT");
        }
        switch (bot) {
            case WestCoast:
                switch (motor) {
                    case LIFT:
                        config = new MotorConfig("LM1", false);
                        break;
                    case INTAKE_LEFT:
                        config = new MotorConfig("lBumperM", false);
                        break;
                    case INTAKE_RIGHT:
                        config = new MotorConfig("rBumperM", true);
                        break;
                }
                break;
            case Mecanum:
                switch (motor) {
                    case LIFT:
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
