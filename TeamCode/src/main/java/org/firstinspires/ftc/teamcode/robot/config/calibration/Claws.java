package org.firstinspires.ftc.teamcode.robot.config.calibration;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.buttons.PAD_BUTTON;
import org.firstinspires.ftc.teamcode.calibration.Subsystem;
import org.firstinspires.ftc.teamcode.robot.CLAWS;
import org.firstinspires.ftc.teamcode.robot.Robot;

public class Claws extends Subsystem {
    private static final String INCREMENT = "CLAW-INCREMENT";
    private static final float MIN_INCREMENT = 0.001f;
    private static final float MAX_INCREMENT = 0.1f;
    private static final float DEFAULT_INCREMENT = 0.01f;

    private static final float MIN_POSITION = 0.0f;
    private static final float MAX_POSITION = 1.0f;
    private static final float DEFAULT_POSITION = 0.5f;

    private enum MM {MIN, MAX}

    private float[][] minMax = new float[CLAWS.values().length][MM.values().length];

    public Claws(OpMode opmode, Robot robot, ButtonHandler buttons) {
        super(opmode, robot, buttons);

        for (CLAWS claw : CLAWS.values()) {
            for (MM m : MM.values()) {
                minMax[claw.ordinal()][m.ordinal()] = DEFAULT_POSITION;
            }
            robot.claws[claw.ordinal()].setPosition(
                    minMax[claw.ordinal()][MM.MIN.ordinal()] = DEFAULT_POSITION);
        }
    }

    public String name() {
        return this.getClass().getSimpleName();
    }

    protected void load() {
        buttons.spinners.add(INCREMENT,
                opmode.gamepad1, PAD_BUTTON.right_bumper, PAD_BUTTON.left_bumper,
                MIN_INCREMENT, DEFAULT_INCREMENT);
        buttons.spinners.setLimit(INCREMENT, MIN_INCREMENT, false);
        buttons.spinners.setLimit(INCREMENT, MAX_INCREMENT, true);

        add(MM.MIN, CLAWS.TOP, PAD_BUTTON.dpad_up, PAD_BUTTON.dpad_down);
        add(MM.MAX, CLAWS.TOP, PAD_BUTTON.dpad_right, PAD_BUTTON.dpad_left);
        add(MM.MIN, CLAWS.BOTTOM, PAD_BUTTON.y, PAD_BUTTON.a);
        add(MM.MAX, CLAWS.BOTTOM, PAD_BUTTON.x, PAD_BUTTON.b);
    }

    protected void unload() {
        for (CLAWS claw : CLAWS.values()) {
            for (MM m : MM.values()) {
                buttons.spinners.remove(clawName(m, claw));
            }
        }
        buttons.spinners.remove(INCREMENT);
    }

    protected void update() {
        for (CLAWS claw : CLAWS.values()) {
            for (MM m : MM.values()) {
                float val = buttons.spinners.getFloat(clawName(m, claw));
                if (val != minMax[claw.ordinal()][m.ordinal()]) {
                    minMax[claw.ordinal()][m.ordinal()] = val;
                    robot.claws[claw.ordinal()].setPosition(val);
                }
            }
        }
    }

    private void add(MM m, CLAWS claw, PAD_BUTTON up, PAD_BUTTON down) {
        String name = clawName(m, claw);
        buttons.spinners.add(name, opmode.gamepad1, up, down,
                INCREMENT, minMax[claw.ordinal()][m.ordinal()]);
        buttons.spinners.setLimit(name, MIN_POSITION, false);
        buttons.spinners.setLimit(name, MAX_POSITION, true);
    }

    private String clawName(MM m, CLAWS claw) {
        return "CLAW-" + claw + "-" + m;
    }
}
