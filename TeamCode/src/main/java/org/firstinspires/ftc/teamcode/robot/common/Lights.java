package org.firstinspires.ftc.teamcode.robot.common;

import org.firstinspires.ftc.teamcode.robot.Robot;

public class Lights {
    // Light constants
    private static final float BRIGHTNESS_FULL = -1.0f;
    private static final float BRIGHTNESS_OFF = 0.0f;
    private static final int FLASH_TIMEOUT = 500;

    // Game timestamps
    public static final int TELEOP_DURATION = 1000 * 60 * 2;
    public static final int ENDGAME_DURATION = 30 * 1000;

    // Light modes
    public enum MODE {
        OFF, ON, AUTO_INIT, AUTO_RUN, TELEOP
    }

    // Run-time
    private final Robot robot;
    private MODE mode = MODE.OFF;
    private long startTime = 0;
    private long endTime = 0;
    private long endgameTime = 0;
    private Flashing flashing = new Flashing();

    private class Flashing {
        public boolean enabled = false;
        public long next = 0;
        public boolean illuminated = false;
        public int timeout = FLASH_TIMEOUT;
    }

    public Lights(Robot robot) {
        this(robot, MODE.OFF);
    }

    public Lights(Robot robot, MODE mode) {
        this.robot = robot;
        this.mode = mode;
    }

    public void start() {
        startTime = System.currentTimeMillis();
        endTime = startTime + TELEOP_DURATION;
        endgameTime = endTime - ENDGAME_DURATION;
    }

    public void loop() {
        loop(null);
    }

    public void loop(MODE newMode) {
        if (newMode != null) {
            this.mode = newMode;
        }

        float brightness = BRIGHTNESS_OFF;
        long now = System.currentTimeMillis();

        // Each mode has its own rules
        switch (mode) {
            case OFF:
                brightness = off();
                break;
            case ON:
                brightness = on();
                break;
            case AUTO_INIT:
                flashing.enabled = true;
                flashing.timeout = FLASH_TIMEOUT;
                break;
            case AUTO_RUN:
                brightness = on();
                break;
            case TELEOP:
                if (now > endTime) {
                    // Off after game end
                    brightness = off();
                } else if (now > endgameTime) {
                    // Flashing during endgame
                    flashing.enabled = true;
                    flashing.timeout = ((int) (endTime - now) / ENDGAME_DURATION) * FLASH_TIMEOUT;
                } else {
                    // Off at all other times
                    brightness = off();
                }
                break;
        }

        // Flash if enabled
        if (flashing.enabled) {
            if (flashing.next < now) {
                flashing.next = now + flashing.timeout;
                flashing.illuminated = !flashing.illuminated;
            }
            brightness = flashing.illuminated ? BRIGHTNESS_FULL : BRIGHTNESS_OFF;
        }

        // Update the lights
        robot.lights.setPower(brightness);
    }

    private float off() {
        flashing.enabled = false;
        return BRIGHTNESS_OFF;
    }

    private float on() {
        flashing.enabled = false;
        return BRIGHTNESS_FULL;
    }
}
