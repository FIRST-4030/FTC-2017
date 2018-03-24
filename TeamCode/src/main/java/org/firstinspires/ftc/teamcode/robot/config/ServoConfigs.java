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
                    case JEWEL_ARM:
                        config = new ServoConfig("Jewel Arm", true, 0.1f, 1.0f);
                        break;
                }
                break;
            case Mecanum:
                switch (servo) {
                    case JEWEL_ARM:
                        config = new ServoConfig("Jewel Arm", true, 0.1f, 1.0f);
                        break;
                }
                break;
            case WestCoastClaw:
                switch (servo) {
                    case JEWEL_ARM:
                        config = new ServoConfig("Jewel Arm", true, 0.1f, 1.0f);
                        break;
                    case CLAW_TOP:
                        config = new ServoConfig("CL1", true, 0.14f, 0.41f);
                        break;
                    case CLAW_BOTTOM:
                        config = new ServoConfig("CL2", true, 0.11f, 0.41f);
                        break;

                }
                break;
        }
        return config;
    }
}
