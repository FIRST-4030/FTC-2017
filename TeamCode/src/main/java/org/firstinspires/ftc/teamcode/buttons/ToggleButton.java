package org.firstinspires.ftc.teamcode.buttons;

public class ToggleButton {
    private boolean active = false;
    private SinglePressButton toggle = new SinglePressButton();

    public ToggleButton(boolean active) {
        this.active = active;
    }

    public ToggleButton() {
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
}
