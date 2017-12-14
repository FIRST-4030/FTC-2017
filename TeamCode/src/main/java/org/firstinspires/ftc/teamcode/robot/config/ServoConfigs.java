package org.firstinspires.ftc.teamcode.robot.config;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.actuators.ServoConfig;
import org.firstinspires.ftc.teamcode.robot.SERVOS;
import org.firstinspires.ftc.teamcode.config.BOT;
import org.firstinspires.ftc.teamcode.config.Configs;

public class ServoConfigs extends Configs {
    public ServoConfigs(HardwareMap map, Telemetry telemetry, BOT bot) {
        super(map, telemetry, bot);
    }

    public ServoFTC init(SERVOS name) {
        ServoConfig config = config(name);
        super.checkConfig(config, name);
        ServoFTC servo = new ServoFTC(map, telemetry, config);
        super.checkAvailable(servo, name);
        return servo;
    }

    public ServoConfig config(SERVOS servo) {
        super.checkBOT();
        checkNull(servo, SERVOS.class.getName());

        ServoConfig config = null;
        switch (bot) {
            case WestCoast:
                switch (servo) {
                    case CLAW_TOP:
                        config = new ServoConfig("CL1", true, 0.67d, 1.0d);
                        break;
                    case CLAW_BOTTOM:
                        config = new ServoConfig("CL2", true, 0.5d, 0.85d);
                        break;
                    case JEWEL_ARM:
                        config = new ServoConfig("Jewel Arm", true, 0.2d, 1.0d); // needs calibration
                        break;
                }
                break;
            case Mecanum:
                switch (servo) {
                    case CLAW_TOP:
                        config = new ServoConfig("CL1", true, 0.22d, 0.66d);
                        break;
                    case CLAW_BOTTOM:
                        config = new ServoConfig("CL2", true, 0.08d, 0.44d);
                        break;
                    case JEWEL_ARM:
                        config = new ServoConfig("Jewel Arm", true, 0.0d, 1.0d); // needs calibration
                        break;
                }
                break;
        }
        return config;
    }
}
