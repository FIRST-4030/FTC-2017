package org.firstinspires.ftc.teamcode.buttons;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ButtonHandler {
    private HashMap<String, Button> buttons = new HashMap<>();

    public void register(String name, Gamepad gamepad, BUTTON button) {
        this.register(name, gamepad, button, BUTTON_TYPE.SINGLE_PRESS);
    }

    // Register a button for consolidated updates
    public void register(String name, Gamepad gamepad, BUTTON button, BUTTON_TYPE type) {

        // Die hard if we're used poorly
        if (name == null || gamepad == null || button == null) {
            throw new NullPointerException("Null or empty name, gamepad or button");
        }

        // Ensure the named button exists, as the compiler can't check
        try {
            Field field = gamepad.getClass().getField(button.name());
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Invalid button: " + button);
        }

        // Create a button of the appropriate type
        Button b = new Button(gamepad, button);
        switch (type) {
            case SINGLE_PRESS:
                b.listener = new SinglePressButton();
                break;
            case TOGGLE:
                b.listener = new ToggleButton();
                break;
        }

        // Register it in the map
        if (buttons.containsKey(name)) {
            System.err.println("De-registering existing button: " + name);
        }
        buttons.put(name, b);
    }

    // Remove named button from the handler
    public void deregister(String name) {
        if (buttons.containsKey(name)) {
            buttons.remove(name);
        }
    }

    // Update all buttons
    public void update() {
        for (Button b : buttons.values()) {
            try {
                Field field = b.gamepad.getClass().getField(b.button.name());
                b.listener.update(field.getBoolean(b.gamepad));
            } catch (Exception e) {
                // We checked this when registering so this shouldn't happen, but log if it does
                System.err.println("Unable to update button: " + b.button);
            }
        }
    }

    public boolean get(String name) {
        if (!buttons.containsKey(name)) {
            System.err.println("Unregistered button name: " + name);
            return false;
        }
        return buttons.get(name).listener.active();
    }

    private class Button {
        public Gamepad gamepad;
        public BUTTON button;
        public ButtonHandlerListener listener;

        public Button(Gamepad gamepad, BUTTON button) {
            this.gamepad = gamepad;
            this.button = button;
        }
    }
}
