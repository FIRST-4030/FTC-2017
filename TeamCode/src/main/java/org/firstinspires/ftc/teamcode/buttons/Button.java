package org.firstinspires.ftc.teamcode.buttons;

public abstract class Button {
    final int LONG_HOLD_TIMEOUT = 400;
    final int AUTOKEY_TIMEOUT = 50;

    protected boolean active = false;
    protected long autokeyTimeout = 0;
    protected long longHeldTimeout = 0;

    public abstract void update(boolean button);

    public boolean active() {
        return active;
    }

    public abstract boolean held();

    // Same as held(), but delayed by LONG_HOLD_TIMEOUT milliseconds
    public boolean heldLong() {
        return held() && longHeldTimeout < System.currentTimeMillis();
    }

    // True when active
    public boolean autokey() {
        if (active()) {
            return true;
        }
        long now = System.currentTimeMillis();
        if (heldLong() && autokeyTimeout < now) {
            autokeyTimeout = now + AUTOKEY_TIMEOUT;
            return true;
        }
        return false;
    }
}
