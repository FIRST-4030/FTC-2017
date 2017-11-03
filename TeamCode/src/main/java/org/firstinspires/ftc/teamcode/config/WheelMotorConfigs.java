package org.firstinspires.ftc.teamcode.config;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.wheels.MotorSide;
import org.firstinspires.ftc.teamcode.wheels.TankDrive;
import org.firstinspires.ftc.teamcode.wheels.TankMotor;

public class WheelMotorConfigs {
    private static BOT bot = null;

    public TankDrive init(HardwareMap map, Telemetry telemetry) {
        TankDrive tank = null;
        for (BOT i : BOT.values()) {
            bot = i;
            tank = new TankDrive(map, config(), encoderIndex(), encoderScale(), telemetry);
            if (tank.isAvailable()) {
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

    private TankMotor[] config() {
        TankMotor[] config = null;
        assert bot != null;
        switch (bot) {
            case FINAL:
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