package org.firstinspires.ftc.teamcode.robot.config;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.actuators.MotorConfig;
import org.firstinspires.ftc.teamcode.robot.MOTORS;
import org.firstinspires.ftc.teamcode.config.BOT;
import org.firstinspires.ftc.teamcode.config.Configs;

public class MotorConfigs extends Configs {
    public MotorConfigs(HardwareMap map, Telemetry telemetry, BOT bot) {
        super(map, telemetry, bot);

    }

    public Motor init(MOTORS name) {
        MotorConfig config = config(name);
        super.checkConfig(config, name);
        Motor motor = new Motor(map, telemetry, config);
        super.checkAvailable(motor, name);
        return motor;
    }

    public MotorConfig config(MOTORS motor) {
        super.checkBOT();
        checkNull(motor, MOTORS.class.getName());

        MotorConfig config = null;
        switch (bot) {
            case WestCoast:
                switch (motor) {
                    case LIFT:
                        config = new MotorConfig("LM1", false);
                        break;
                    case INTAKE_L:
                        config = new MotorConfig("L_Intake", false);
                        break;
                    case INTAKE_R:
                        config = new MotorConfig("R_Intake", true);
                        break;
                }
                break;
            case Mecanum:
                switch (motor) {
                    case LIFT:
                        config = new MotorConfig("LM1", false);
                        break;
                    case INTAKE_L:
                        config = new MotorConfig("L_Intake", true);
                        break;
                    case INTAKE_R:
                        config = new MotorConfig("R_Intake", true);
                        break;
                }
                break;
        }
        return config;
    }
}
