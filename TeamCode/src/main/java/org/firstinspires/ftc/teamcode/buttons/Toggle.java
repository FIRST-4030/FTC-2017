package org.firstinspires.ftc.teamcode.buttons;

public class Toggle extends Button {
    private final SinglePress toggle = new SinglePress();

    public void update(boolean button) {
        toggle.update(button);

        // Toggle on button down events
        if (toggle.active()) {
            active = !active;
            longHeldTimeout = System.currentTimeMillis() + LONG_HOLD_TIMEOUT;
        }
    }

    // Toggle natively simulates holding
    public boolean held() {
        return active();
    }
}
