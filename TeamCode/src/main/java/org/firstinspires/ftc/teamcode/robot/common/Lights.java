package org.firstinspires.ftc.teamcode.robot.common;

import org.firstinspires.ftc.teamcode.robot.Robot;

public class Lights {
    // Light constants
    private static final float BRIGHTNESS_FULL = 1.0f;
    private static final float BRIGHTNESS_OFF = 0.0f;
    private static final int FLASH_TIMEOUT = 500;

    // Game timestamps
    public static final int TELEOP_DURATION = 1000 * 60 * 2;
    public static final int ENDGAME_DURATION = 30 * 1000;

    // Run-time
    private final Robot robot;
    private long startTime = 0;
    private long endTime = 0;
    private long endgameTime = 0;

    // Flashing state tracking
    private boolean flashOn = false;
    private long flashTimeout = 0;

    public Lights(Robot robot) {
        this.robot = robot;
    }

    public void start() {
        startTime = System.currentTimeMillis();
        endTime = startTime + TELEOP_DURATION;
        endgameTime = endTime - ENDGAME_DURATION;
    }

    public void loop() {
        long now = System.currentTimeMillis();
        float brightness = BRIGHTNESS_OFF;

        // Flash during endgame
        if (now > endgameTime) {
            if (flashTimeout < now) {
                // Increase flashing rate as endgame proceeds
                long timeout = ((endTime - now) / ENDGAME_DURATION) * FLASH_TIMEOUT;
                flashTimeout = now + timeout;
                flashOn = !flashOn;
            }
            if (flashOn) {
                brightness = BRIGHTNESS_FULL;
            }
        } else {
            // Other things
        }

        // Update the lights
        robot.lights.setPower(brightness);
    }
}
