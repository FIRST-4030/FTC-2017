package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.field.VuforiaConfigs;
import org.firstinspires.ftc.teamcode.config.BOT;
import org.firstinspires.ftc.teamcode.robot.config.GyroConfigs;
import org.firstinspires.ftc.teamcode.robot.config.MotorConfigs;
import org.firstinspires.ftc.teamcode.robot.config.ServoConfigs;
import org.firstinspires.ftc.teamcode.robot.config.SwitchConfigs;
import org.firstinspires.ftc.teamcode.robot.config.WheelsConfigs;
import org.firstinspires.ftc.teamcode.sensors.gyro.Gyro;
import org.firstinspires.ftc.teamcode.sensors.switches.Switch;
import org.firstinspires.ftc.teamcode.vuforia.VuforiaFTC;
import org.firstinspires.ftc.teamcode.wheels.Wheels;

public class Robot {
    public static Robot robot = null;

    public BOT bot = null;
    public Wheels wheels = null;
    public Motor lift = null;
    public ServoFTC[] claws = null;
    public Gyro gyro = null;
    public Switch liftSwitch = null;
    public VuforiaFTC vuforia = null;
    public ServoFTC jewelArm = null;

    public final HardwareMap map;
    public final Telemetry telemetry;

    public Robot(HardwareMap map, Telemetry telemetry) {
        this(map, telemetry, null);
    }

    public Robot(HardwareMap map, Telemetry telemetry, BOT bot) {
        robot = this;
        this.map = map;
        this.telemetry = telemetry;
        if (bot == null) {
            bot = detectBot();
        }
        this.bot = bot;

        init();
    }

    public BOT detectBot() {
        // Try WheelsConfigs from each bot until something succeeds
        bot = null;
        for (BOT b : BOT.values()) {
            WheelsConfigs wheels = new WheelsConfigs(map, telemetry, b);
            Wheels w = wheels.init();
            if (w != null && w.isAvailable()) {
                bot = b;
                break;
            }
        }
        if (bot == null) {
            bot = BOT.values()[0];
            telemetry.log().add("BOT detection failed. Default: " + bot);
        }
        if (bot.ordinal() != 0) {
            telemetry.log().add("Using BOT: " + bot);
        }
        return bot;
    }

    private void init() {
        GyroConfigs gyros = new GyroConfigs(map, telemetry, bot);
        WheelsConfigs wheels = new WheelsConfigs(map, telemetry, bot);
        MotorConfigs motors = new MotorConfigs(map, telemetry, bot);
        ServoConfigs servos = new ServoConfigs(map, telemetry, bot);
        SwitchConfigs switches = new SwitchConfigs(map, telemetry, bot);

        this.wheels = wheels.init();
        this.wheels.stop();

        liftSwitch = switches.init(SWITCHES.LIFT);
        lift = motors.init(MOTORS.LIFT);
        lift.stop();

        claws = new ServoFTC[CLAWS.values().length];
        claws[CLAWS.TOP.ordinal()] = servos.init(SERVOS.CLAW_TOP);
        claws[CLAWS.BOTTOM.ordinal()] = servos.init(SERVOS.CLAW_BOTTOM);
        for (ServoFTC claw : claws) {
            claw.min();
        }

        jewelArm = servos.init(SERVOS.JEWEL_ARM);
        jewelArm.min();

        gyro = gyros.init();

        vuforia = new VuforiaFTC(VuforiaConfigs.AssetName, VuforiaConfigs.TargetCount,
                VuforiaConfigs.Field(), VuforiaConfigs.Bot());
    }
}
