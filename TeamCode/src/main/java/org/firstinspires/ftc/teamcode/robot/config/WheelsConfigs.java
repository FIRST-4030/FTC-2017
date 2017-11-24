package org.firstinspires.ftc.teamcode.robot.config;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.config.BOT;
import org.firstinspires.ftc.teamcode.config.Configs;
import org.firstinspires.ftc.teamcode.wheels.DRIVE_TYPE;
import org.firstinspires.ftc.teamcode.wheels.MOTOR_END;
import org.firstinspires.ftc.teamcode.wheels.MOTOR_SIDE;
import org.firstinspires.ftc.teamcode.wheels.WheelsConfig;
import org.firstinspires.ftc.teamcode.wheels.TankDrive;
import org.firstinspires.ftc.teamcode.wheels.WheelMotor;
import org.firstinspires.ftc.teamcode.wheels.Wheels;

public class WheelsConfigs extends Configs {
    public WheelsConfigs(HardwareMap map, Telemetry telemetry, BOT bot) {
        super(map, telemetry, bot);
    }

    public Wheels init() {
        WheelsConfig config = config();
        super.checkConfig(config);
        Wheels wheels = null;
        switch (config.type) {
            case TANK:
                wheels = new TankDrive(map, telemetry, config);
                break;
            case MECANUM:
                break;
        }
        super.checkAvailable(wheels);
        return wheels;
    }

    public WheelsConfig config() {
        super.checkBOT();

        WheelMotor[] motors;
        WheelsConfig config = null;
        switch (bot) {
            case WestCoast:
                motors = new WheelMotor[4];
                motors[0] = new WheelMotor("ML1", MOTOR_SIDE.LEFT, true);
                motors[1] = new WheelMotor("MR1", MOTOR_SIDE.RIGHT);
                motors[2] = new WheelMotor("ML2", MOTOR_SIDE.LEFT, true);
                motors[3] = new WheelMotor("MR2", MOTOR_SIDE.RIGHT);
                config = new WheelsConfig(DRIVE_TYPE.TANK, motors, 0, 1.0d);
                break;
            case Mecanum:
                motors = new WheelMotor[4];
                motors[0] = new WheelMotor("LF", MOTOR_SIDE.LEFT, MOTOR_END.FRONT);
                motors[1] = new WheelMotor("LR", MOTOR_SIDE.LEFT, MOTOR_END.BACK, true);
                motors[2] = new WheelMotor("RF", MOTOR_SIDE.RIGHT, MOTOR_END.FRONT);
                motors[3] = new WheelMotor("RR", MOTOR_SIDE.RIGHT, MOTOR_END.BACK, true);
                config = new WheelsConfig(DRIVE_TYPE.TANK, motors, 0, 1.0d);
                break;
        }
        return config;
    }
}
