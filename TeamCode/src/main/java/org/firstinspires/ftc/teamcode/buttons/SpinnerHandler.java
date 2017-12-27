package org.firstinspires.ftc.teamcode.buttons;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.robot.Robot;

import java.util.HashMap;

public class SpinnerHandler {
    private final ButtonHandler parent;
    private final Robot robot;
    private final HashMap<String, Spinner> spinners;
    private boolean disabled = true;

    public SpinnerHandler(ButtonHandler parent, Robot robot) {
        if (parent == null) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + "Null handler");
        }
        this.robot = robot;
        this.parent = parent;
        spinners = new HashMap<>();
    }

    public void setEnable(boolean enable) {
        this.disabled = !enable;
    }

    public void handle() {
        if (disabled) {
            return;
        }

        for (Spinner spinner : spinners.values()) {
            if (parent.get(spinner.up())) {
                spinner.increment();
            } else if (parent.get(spinner.down())) {
                spinner.decrement();
            }

            // Publish telemetry
            telemetry(spinner);
        }
    }

    public void telemetry(Spinner spinner) {
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
        robot.telemetry.addData(label, cls.cast(spinner.value));
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
                set(name, (Integer) (int) value);
                break;
            case DOUBLE:
                set(name, (Double) (double) value);
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
                set(name, (Integer) (int) value);
                break;
            case DOUBLE:
                set(name, (Double) (double) value);
                break;
        }
    }

    private void set(String name, Object val) {
        Spinner spinner = spinners.get(name);
        if (spinner == null) {
            return;
        }
        spinner.value = spinner.limits(val);
    }

    public void add(String name, SPINNER_TYPE type,
                    Gamepad pad, BUTTON up, BUTTON down,
                    Object increment, Object value) {
        add(name, type, pad, up, down, increment, value, null, null);
    }

    public void add(String name, SPINNER_TYPE type,
                    Gamepad pad, BUTTON up, BUTTON down,
                    Object increment, Object value,
                    Object min, Object max) {
        if (name == null || type == null ||
                pad == null || up == null || down == null ||
                increment == null || value == null) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": Null parameter: " +
                    (name == null ? "name" : "") +
                    (type == null ? "type" : "") +
                    (pad == null ? "pad" : "") +
                    (up == null ? "up" : "") +
                    (down == null ? "down" : "") +
                    (increment == null ? "increment" : "") +
                    (value == null ? "value" : "")
            );
        }

        // Main object
        if (spinners.containsKey(name)) {
            robot.telemetry.log().add("De-registering existing spinner: " + name);
        }
        Spinner spinner = new Spinner(this, name, type, pad, up, down, increment, value, min, max);
        spinners.put(name, spinner);

        // Register buttons
        parent.register(spinner.up(), pad, up);
        parent.register(spinner.down(), pad, down);

        // Enable whenever something is added
        setEnable(true);
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

        private SpinnerHandler handler;
        private String name;
        private SPINNER_TYPE type;
        private Gamepad gamepad;
        private BUTTON up;
        private BUTTON down;
        private Object value;
        private Object increment;
        private boolean incrementIsName;
        private Object min;
        private Object max;

        protected Spinner(SpinnerHandler handler, String name, SPINNER_TYPE type,
                          Gamepad gamepad, BUTTON up, BUTTON down,
                          Object increment, Object value,
                          Object min, Object max) {

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
                checkType(cls, increment, "increment");
                checkType(cls, value, "value");
                checkType(cls, min, "min");
                checkType(cls, max, "max");
            }

            this.handler = handler;
            this.name = name;
            this.type = type;
            this.gamepad = gamepad;
            this.up = up;
            this.down = down;
            this.increment = increment;
            this.value = value;
            this.min = min;
            this.max = max;
        }

        private void checkType(Class cls, Object obj, String name) {
            if (obj != null && !cls.isInstance(obj)) {
                throw new IllegalArgumentException(this.getClass().getSimpleName() +
                        ": Parameter '" + name + "' for type " + type +
                        " must be of class: " + cls.getName());
            }
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
                        inc = handler.getInt((String) increment);
                        break;
                    case DOUBLE:
                        inc = handler.getDouble((String) increment);
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

            Object val = value;
            switch (type) {
                case INT:
                    val = (Integer) value + (Integer) inc;
                    break;
                case DOUBLE:
                    val = (Double) value + (Double) inc;
                    break;
            }
            value = limits(val);
        }

        protected Object limits(Object val) {
            if (min != null) {
                switch (type) {
                    case INT:
                        val = Math.min((Integer) val, (Integer) min);
                        break;
                    case DOUBLE:
                        val = Math.min((Double) val, (Double) min);
                        break;
                }
            }
            if (max != null) {
                switch (type) {
                    case INT:
                        val = Math.max((Integer) val, (Integer) max);
                        break;
                    case DOUBLE:
                        val = Math.max((Double) val, (Double) max);
                        break;
                }
            }
            return val;
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
