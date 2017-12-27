package org.firstinspires.ftc.teamcode.buttons;

public class Toggle implements ButtonType {
    private boolean active = false;
    private final SinglePress toggle = new SinglePress();

    public Toggle(boolean active) {
        this.active = active;
    }

    public Toggle() {
        this(false);
    }

    public void update(boolean button) {
        toggle.update(button);

        // Toggle on button down events
        if (toggle.active()) {
            active = !active;
        }
    }

    public boolean active() {
        return active;
    }

    // Toggle natively simulates holding
    public boolean held() {
        return active();
    }
}
