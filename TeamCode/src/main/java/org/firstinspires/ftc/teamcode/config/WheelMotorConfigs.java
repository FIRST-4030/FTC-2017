package org.firstinspires.ftc.teamcode.config;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.wheels.MotorSide;
import org.firstinspires.ftc.teamcode.wheels.TankDrive;
import org.firstinspires.ftc.teamcode.wheels.TankMotor;

public class WheelMotorConfigs {
    private HardwareMap map = null;
    private Telemetry telemetry = null;
    private BOT bot = null;

    public WheelMotorConfigs(HardwareMap map, Telemetry telemetry, BOT bot) {
        this.map = map;
        this.telemetry = telemetry;
        this.bot = bot;
    }

    public WheelMotorConfigs(HardwareMap map, Telemetry telemetry) {
        this(map, telemetry, null);
    }

    public TankDrive init() {
        TankDrive tank = null;
        if (bot != null) {
            tank = new TankDrive(map, config(bot), encoderIndex(), encoderScale(), telemetry);
        }
        for (BOT i : BOT.values()) {
            BOT b = i;
            tank = new TankDrive(map, config(b), encoderIndex(), encoderScale(), telemetry);
            if (tank.isAvailable()) {
                bot = b;
                if (bot.ordinal() != 0) {
                    telemetry.log().add("NOTICE: Using wheel config: " + bot);
                }
                break;
            }
        }
        assert tank != null;
        if (!tank.isAvailable()) {
            telemetry.log().add("ERROR: Unable to initialize wheels");
        }
        return tank;
    }

    private static TankMotor[] config(BOT b) {
        TankMotor[] config = null;
        assert b != null;
        switch (b) {
            case FINAL:
            case CALIBRATION:
                config = FinalBot();
                break;
            case CODE:
                config = CodeBot();
                break;
        }
        return config;
    }

    private double encoderScale() {
        double scale = 1.0;
        assert bot != null;
        switch (bot) {
            case FINAL:
            case CALIBRATION:
                scale = FinalBotEncoderScale;
                break;
            case CODE:
                scale = CodeBotEncoderScale;
                break;
        }
        return scale;
    }

    private int encoderIndex() {
        int index = 0;
        assert bot != null;
        switch (bot) {
            case FINAL:
            case CALIBRATION:
                index = FinalBotEncoder;
                break;
            case CODE:
                index = CodeBotEncoder;
                break;
        }
        return index;
    }

    /*
     *  Per-machine config follows.
     *
     *  Everything above is just syntax sugar.
     *  No changes are needed above unless the enum BOT changes.
     */

    private static final double CodeBotEncoderScale = 4.7 / 3;
    private static final int CodeBotEncoder = 0;

    public static TankMotor[] CodeBot() {
        TankMotor motors[] = new TankMotor[2];
        motors[0] = new TankMotor("L", MotorSide.LEFT);
        motors[1] = new TankMotor("R", MotorSide.RIGHT, true);
        return motors;
    }

    private static final double FinalBotEncoderScale = 1.0;
    private static final int FinalBotEncoder = 0;

    private static TankMotor[] FinalBot() {
        TankMotor motors[] = new TankMotor[4];
        motors[0] = new TankMotor("ML1", MotorSide.LEFT, true);
        motors[1] = new TankMotor("MR1", MotorSide.RIGHT);
        motors[2] = new TankMotor("ML2", MotorSide.LEFT, true);
        motors[3] = new TankMotor("MR2", MotorSide.RIGHT);
        return motors;
    }
}
