package org.firstinspires.ftc.teamcode.buttons;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.HashMap;

public class SpinnerHandler implements ButtonHandlerListener {
    private final ButtonHandler parent;
    private final Telemetry telemetry;
    private final HashMap<String, Spinner> spinners;

    public SpinnerHandler(ButtonHandler parent, Telemetry telemetry) {
        if (parent == null) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + "Null listener");
        }
        this.telemetry = telemetry;
        this.parent = parent;
        spinners = new HashMap<>();
    }

    public void onButtonHandler() {
        for (Spinner spinner : spinners.values()) {
            if (parent.get(spinner.up())) {
                spinner.increment();
            } else if (parent.get(spinner.down())) {
                spinner.decrement();
            }
        }
    }

    public void telemetry() {
        for (Spinner spinner : spinners.values()) {
            Class cls = null;
            switch (spinner.type) {
                case INT:
                    cls = Integer.class;
                    break;
                case DOUBLE:
                    cls = Double.class;
                    break;
            }
            String label = this.getClass().getSimpleName().charAt(0) +
                    cls.getSimpleName().charAt(0) + ":" + spinner.name;
            telemetry.addData(label, cls.cast(spinner.value));
        }
    }

    public double get(String name) {
        return getDouble(name);
    }

    public double getDouble(String name) {
        Spinner spinner = spinners.get(name);
        if (spinner == null) {
            return 0.0d;
        }

        // This style is terrible but easy to read
        switch (spinner.type) {
            case INT:
                return ((Integer) spinner.value).doubleValue();
            case DOUBLE:
                return (Double) spinner.value;
        }
        return 0.0d;
    }

    public int getInt(String name) {
        Spinner spinner = spinners.get(name);
        if (spinner == null) {
            return 0;
        }

        // This style is terrible but easy to read
        switch (spinner.type) {
            case INT:
                return (Integer) spinner.value;
            case DOUBLE:
                return ((Double) spinner.value).intValue();
        }
        return 0;
    }

    public void set(String name, int value) {
        Spinner spinner = spinners.get(name);
        if (spinner == null) {
            return;
        }
        switch (spinner.type) {
            case INT:
                spinner.value = value;
                break;
            case DOUBLE:
                spinner.value = (double) value;
                break;
        }
    }

    public void set(String name, double value) {
        Spinner spinner = spinners.get(name);
        if (spinner == null) {
            return;
        }
        switch (spinner.type) {
            case INT:
                spinner.value = (int) value;
                break;
            case DOUBLE:
                spinner.value = value;
                break;
        }
    }

    private void set(String name, Object value) {
        Spinner spinner = spinners.get(name);
        if (spinner == null) {
            return;
        }
        spinner.value = value;
    }

    public void add(String name, SPINNER_TYPE type,
                    Gamepad pad, BUTTON up, BUTTON down,
                    Object increment, Object value) {
        if (name == null || type == null ||
                pad == null || up == null || down == null ||
                increment == null) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": Null parameters: " +
                    (name == null ? "name" : "") +
                    (type == null ? "type" : "") +
                    (pad == null ? "pad" : "") +
                    (up == null ? "up" : "") +
                    (down == null ? "down" : "") +
                    (increment == null ? "increment" : "")
            );
        }
        if (value == null) {
            switch (type) {
                case INT:
                    value = 0;
                    break;
                case DOUBLE:
                    value = 0.0d;
                    break;
            }
        }

        // Main object
        Spinner spinner = new Spinner(this, name, type, pad, up, down, increment, value);
        if (spinners.containsKey(name)) {
            telemetry.log().add("De-registering existing spinner: " + name);
        }
        spinners.put(name, spinner);

        // Register buttons
        parent.register(spinner.up(), pad, up);
        parent.register(spinner.down(), pad, down);
    }

    public void remove(String name) {
        if (spinners.containsKey(name)) {
            spinners.remove(name);
        }
    }

    protected String upName(Spinner spinner) {
        return "__SPINNER_" + spinner.name + "_UP";
    }

    protected String downName(Spinner spinner) {
        return "__SPINNER_" + spinner.name + "_DOWN";
    }

    protected class Spinner {
        protected static final String namespace = "__SPINNER";

        private ButtonHandlerListener listener;
        private String name;
        private SPINNER_TYPE type;
        private Gamepad gamepad;
        private BUTTON up;
        private BUTTON down;
        private Object value;
        private Object increment;
        private boolean incrementIsName;

        protected Spinner(ButtonHandlerListener listener, String name, SPINNER_TYPE type,
                          Gamepad gamepad, BUTTON up, BUTTON down,
                          Object increment, Object value) {

            if (increment.getClass().isInstance(String.class)) {
                incrementIsName = true;
            } else {
                incrementIsName = false;
                Class cls = null;
                switch (type) {
                    case INT:
                        cls = Integer.class;
                        break;
                    case DOUBLE:
                        cls = Double.class;
                        break;
                }
                if (!cls.isInstance(increment)) {
                    throw new IllegalArgumentException(this.getClass().getSimpleName() +
                            ": increment for type " + type +
                            " must be of class: " + cls.getName());
                }
                if (!cls.isInstance(value)) {
                    throw new IllegalArgumentException(this.getClass().getSimpleName() +
                            ": value for type " + type +
                            " must be of class: " + cls.getName());
                }
            }

            this.listener = listener;
            this.name = name;
            this.type = type;
            this.gamepad = gamepad;
            this.up = up;
            this.down = down;
            this.increment = increment;
            this.value = value;
        }

        protected void increment() {
            increment(true);
        }

        protected void decrement() {
            increment(false);
        }

        protected void increment(boolean positive) {
            Object inc = increment;
            if (incrementIsName) {
                switch (type) {
                    case INT:
                        inc = listener.getInt((String) increment);
                        break;
                    case DOUBLE:
                        inc = listener.getDouble((String) increment);
                        break;
                }
            }

            if (!positive) {
                switch (type) {
                    case INT:
                        inc = (Integer) inc * -1;
                        break;
                    case DOUBLE:
                        inc = (Double) inc * -1.0d;
                        break;
                }
            }

            switch (type) {
                case INT:
                    value = (Integer) value + (Integer) inc;
                    break;
                case DOUBLE:
                    value = (Double) value + (Double) inc;
                    break;
            }
        }

        public String up() {
            return buttonName("UP");
        }

        public String down() {
            return buttonName("DOWN");
        }

        private String buttonName(String postfix) {
            return namespace + ":" + name + ":" + postfix;
        }
    }
}
