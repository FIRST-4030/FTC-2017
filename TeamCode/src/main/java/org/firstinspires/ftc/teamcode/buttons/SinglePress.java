package org.firstinspires.ftc.teamcode.buttons;

public class SinglePress extends Button {
    private boolean released = true;

    public void update(boolean button) {

        // Reset the state on any update
        if (active) {
            active = false;
        }

        // Remain inactive while the button is down
        if (!released && button) {
            return;
        }

        // Set released when the button first comes up
        if (!button && !released) {
            released = true;
            return;
        }

        // Active only when the button first goes down
        if (button) {
            active = true;
            released = false;
            longHeldTimeout = System.currentTimeMillis() + LONG_HOLD_TIMEOUT;
        }
    }

    // Released is set when the button is up
    public boolean held() {
        return !released;
    }
}
