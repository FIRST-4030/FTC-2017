package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.robot.configs.BOT;
import org.firstinspires.ftc.teamcode.robot.configs.MotorConfigs;
import org.firstinspires.ftc.teamcode.robot.configs.ServoConfigs;
import org.firstinspires.ftc.teamcode.robot.configs.WheelConfigs;
import org.firstinspires.ftc.teamcode.sensors.Gyro;
import org.firstinspires.ftc.teamcode.sensors.Switch;
import org.firstinspires.ftc.teamcode.wheels.Wheels;

public class Robot {
    public BOT bot = null;
    public Wheels wheels = null;
    public Motor lift = null;
    public ServoFTC[] claws = null;
    public Motor[] intakes = null;
    public ServoFTC[] intakeArms = null;
    public Gyro gyro = null;
    public Switch liftSwitch = null;

    private HardwareMap map;
    private Telemetry telemetry;

    public Robot(HardwareMap map, Telemetry telemetry) {
        this(map, telemetry, null);
    }

    public Robot(HardwareMap map, Telemetry telemetry, BOT bot) {
        this.map = map;
        this.telemetry = telemetry;
        if (bot == null) {
            bot = detectBot();
        }
        this.bot = bot;

        init();
    }

    public BOT detectBot() {
        // Try all motors from each bot until something matches
        bot = BOT.values()[0];
        for (BOT b : BOT.values()) {
            boolean failed = false;
            for (MOTORS name : MOTORS.values()) {
                Motor motor = new Motor(map, telemetry, MotorConfigs.config(name, b));
                if (!motor.isAvailable()) {
                    failed = true;
                    break;
                }
            }
            if (!failed) {
                bot = b;
                break;
            }
        }

        if (bot.ordinal() != 0) {
            telemetry.log().add("Using BOT " + bot);
        }
        return bot;
    }

    private void init() {
        WheelConfigs wheels = new WheelConfigs(map, telemetry, bot);
        MotorConfigs motors = new MotorConfigs(map, telemetry, bot);
        ServoConfigs servos = new ServoConfigs(map, telemetry, bot);

        this.wheels = wheels.init();
        this.wheels.stop();

        lift = motors.init(MOTORS.LIFT);
        lift.stop();

        claws = new ServoFTC[CLAWS.values().length];
        claws[CLAWS.TOP.ordinal()] = servos.init(SERVOS.CLAW_TOP);
        claws[CLAWS.BOTTOM.ordinal()] = servos.init(SERVOS.CLAW_BOTTOM);
        for (ServoFTC claw : claws) {
            claw.min();
        }

        intakes = new Motor[INTAKES.values().length];
        intakes[INTAKES.RIGHT.ordinal()] = motors.init(MOTORS.INTAKE_RIGHT);
        intakes[INTAKES.LEFT.ordinal()] = motors.init(MOTORS.INTAKE_LEFT);
        for (Motor intake : intakes) {
            intake.stop();
        }

        intakeArms = new ServoFTC[INTAKES.values().length];
        intakeArms[INTAKES.RIGHT.ordinal()] = servos.init(SERVOS.INTAKE_RIGHT);
        intakeArms[INTAKES.LEFT.ordinal()] = servos.init(SERVOS.INTAKE_LEFT);
        for (ServoFTC intake : intakeArms) {
            intake.min();
        }

        gyro = new Gyro(map, telemetry, "imu");
        liftSwitch = new Switch(map, telemetry, "LS1");
    }
}
