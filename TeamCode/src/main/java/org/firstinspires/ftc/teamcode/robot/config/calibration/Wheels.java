package org.firstinspires.ftc.teamcode.robot.config.calibration;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.buttons.BUTTON_TYPE;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.buttons.PAD_BUTTON;
import org.firstinspires.ftc.teamcode.calibration.Subsystem;
import org.firstinspires.ftc.teamcode.driveto.RatePID;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.utils.Round;
import org.firstinspires.ftc.teamcode.wheels.MOTOR_SIDE;

public class Wheels extends Subsystem {
    private static final String INCREMENT = "WHEELS-INCREMENT";
    private static final float MIN_INCREMENT = 0.001f;
    private static final float MAX_INCREMENT = 1.0f;
    private static final float DEFAULT_INCREMENT = 0.01f;

    private static final String SPEED = "WHEELS-SPEED";
    private static final float SPEED_INCREMENT = 0.01f;
    private static final float SPEED_MIN = 0.01f;
    private static final float SPEED_MAX = 1.0f;
    private static final float SPEED_DEFAULT = 0.5f;
    private static final String DRIVE = "WHEELS-DRIVE";
    private static final String REVERSE = "WHEELS-REVERSE";
    private static final String RAW = "WHEELS-RAW";

    private enum PIDVal {P, I}

    private final RatePID pids[] = new RatePID[MOTOR_SIDE.values().length];
    private final float[][] vals = new float[MOTOR_SIDE.values().length][PIDVal.values().length];
    private final float rate[] = new float[MOTOR_SIDE.values().length];
    private final float max[] = new float[MOTOR_SIDE.values().length];

    public Wheels(OpMode opmode, Robot robot, ButtonHandler buttons) {
        super(opmode, robot, buttons);

        for (MOTOR_SIDE side : MOTOR_SIDE.values()) {
            pids[side.ordinal()] = robot.wheels.getPID(side, null);
            vals[side.ordinal()][PIDVal.P.ordinal()] = pids[side.ordinal()].params.P;
            vals[side.ordinal()][PIDVal.I.ordinal()] = pids[side.ordinal()].params.I;
        }
    }

    public String name() {
        return this.getClass().getSimpleName();
    }

    public void load() {
        robot.wheels.resetEncoder();

        buttons.register(DRIVE, opmode.gamepad1, PAD_BUTTON.right_trigger);
        buttons.register(RAW, opmode.gamepad1, PAD_BUTTON.left_trigger);
        buttons.register(REVERSE, opmode.gamepad1, PAD_BUTTON.back, BUTTON_TYPE.TOGGLE);

        buttons.spinners.add(SPEED,
                opmode.gamepad1, PAD_BUTTON.right_stick_y, PAD_BUTTON.left_stick_y,
                SPEED_INCREMENT, SPEED_DEFAULT);
        buttons.spinners.setLimit(SPEED, SPEED_MIN, false);
        buttons.spinners.setLimit(SPEED, SPEED_MAX, true);

        buttons.spinners.add(INCREMENT,
                opmode.gamepad1, PAD_BUTTON.right_bumper, PAD_BUTTON.left_bumper,
                MIN_INCREMENT, DEFAULT_INCREMENT);
        buttons.spinners.setLimit(INCREMENT, MIN_INCREMENT, false);
        buttons.spinners.setLimit(INCREMENT, MAX_INCREMENT, true);

        add(MOTOR_SIDE.LEFT, PIDVal.P, PAD_BUTTON.dpad_up, PAD_BUTTON.dpad_down);
        add(MOTOR_SIDE.LEFT, PIDVal.I, PAD_BUTTON.dpad_right, PAD_BUTTON.dpad_left);
        add(MOTOR_SIDE.RIGHT, PIDVal.P, PAD_BUTTON.y, PAD_BUTTON.a);
        add(MOTOR_SIDE.RIGHT, PIDVal.I, PAD_BUTTON.x, PAD_BUTTON.b);
    }

    public void unload() {
        buttons.deregister(DRIVE);
        buttons.deregister(REVERSE);
        buttons.deregister(RAW);
        buttons.spinners.remove(SPEED);

        for (MOTOR_SIDE side : MOTOR_SIDE.values()) {
            for (PIDVal p : PIDVal.values()) {
                buttons.spinners.remove(pidName(side, p));
            }
        }
        buttons.spinners.remove(INCREMENT);
    }

    public void loop() {
        if (buttons.held(RAW)) {
            robot.wheels.setPowerRaw(-opmode.gamepad1.left_stick_y, MOTOR_SIDE.LEFT);
            robot.wheels.setPowerRaw(-opmode.gamepad1.right_stick_y, MOTOR_SIDE.RIGHT);
        } else if (buttons.held(DRIVE)) {
            float speed = buttons.spinners.getFloat(SPEED);
            if (buttons.get(REVERSE)) {
                speed *= -1;
            }
            robot.wheels.setSpeed(speed);
        } else {
            robot.wheels.stop();
        }

        for (MOTOR_SIDE side : MOTOR_SIDE.values()) {
            for (PIDVal p : PIDVal.values()) {
                float val = buttons.spinners.getFloat(pidName(side, p));
                if (val != vals[side.ordinal()][p.ordinal()]) {
                    vals[side.ordinal()][p.ordinal()] = val;
                }
            }
            pids[side.ordinal()].params.P = vals[side.ordinal()][PIDVal.P.ordinal()];
            pids[side.ordinal()].params.I = vals[side.ordinal()][PIDVal.I.ordinal()];
        }

        for (MOTOR_SIDE side : MOTOR_SIDE.values()) {
            robot.telemetry.addData("Encoder-" + side, robot.wheels.getEncoder(side));
        }
        for (MOTOR_SIDE side : MOTOR_SIDE.values()) {
            rate[side.ordinal()] = Math.abs(robot.wheels.getRate(side));
            robot.telemetry.addData("Rate-" + side, Round.truncate(rate[side.ordinal()]));
        }
        for (MOTOR_SIDE side : MOTOR_SIDE.values()) {
            max[side.ordinal()] = Math.max(rate[side.ordinal()], max[side.ordinal()]);
            robot.telemetry.addData("Max Rate-" + side, Round.truncate(max[side.ordinal()]));
        }
    }

    private String pidName(MOTOR_SIDE side, PIDVal p) {
        return "WHEELS-" + p + "-" + side;
    }

    private void add(MOTOR_SIDE side, PIDVal p, PAD_BUTTON up, PAD_BUTTON down) {
        buttons.spinners.add(pidName(side, p), opmode.gamepad1, up, down,
                INCREMENT, vals[side.ordinal()][p.ordinal()]);
    }
}
