package org.firstinspires.ftc.teamcode.robot.config;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.config.BOT;
import org.firstinspires.ftc.teamcode.config.Configs;
import org.firstinspires.ftc.teamcode.driveto.PIDParams;
import org.firstinspires.ftc.teamcode.wheels.DRIVE_TYPE;
import org.firstinspires.ftc.teamcode.wheels.MOTOR_END;
import org.firstinspires.ftc.teamcode.wheels.MOTOR_SIDE;
import org.firstinspires.ftc.teamcode.wheels.MecanumDrive;
import org.firstinspires.ftc.teamcode.wheels.WheelsConfig;
import org.firstinspires.ftc.teamcode.wheels.TankDrive;
import org.firstinspires.ftc.teamcode.wheels.WheelMotor;
import org.firstinspires.ftc.teamcode.wheels.Wheels;

public class WheelsConfigs extends Configs {
    private final static float DERATE = 1.0f;
    private final static float WC_MAX_RATE = 2.50f * DERATE;
    private final static float WC_TICKS_PER_MM = 3.2f;

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
                wheels = new MecanumDrive(map, telemetry, config);
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
                motors[0] = new WheelMotor("ML1", MOTOR_SIDE.LEFT, true,
                        new PIDParams(0.318f, 0.2f, 0.0f), WC_TICKS_PER_MM, WC_MAX_RATE);
                motors[1] = new WheelMotor("MR1", MOTOR_SIDE.RIGHT, true,
                        new PIDParams(0.29f, 0.25f, 0.0f), WC_TICKS_PER_MM, WC_MAX_RATE);
                motors[2] = new WheelMotor("ML2", MOTOR_SIDE.LEFT, true);
                motors[3] = new WheelMotor("MR2", MOTOR_SIDE.RIGHT, true);
                config = new WheelsConfig(DRIVE_TYPE.TANK, motors);
                break;
            case Mecanum:
                motors = new WheelMotor[4];
                motors[0] = new WheelMotor("FL", MOTOR_SIDE.LEFT, MOTOR_END.FRONT, false,
                        new PIDParams(0.1f, 0.01f, 0.0f), 1.0f, 1.0f);
                motors[1] = new WheelMotor("BL", MOTOR_SIDE.LEFT, MOTOR_END.BACK, false);
                motors[2] = new WheelMotor("FR", MOTOR_SIDE.RIGHT, MOTOR_END.FRONT, true);
                motors[3] = new WheelMotor("BR", MOTOR_SIDE.RIGHT, MOTOR_END.BACK, true);
                config = new WheelsConfig(DRIVE_TYPE.MECANUM, motors);
                break;
        }
        return config;
    }
}
