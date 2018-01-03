package org.firstinspires.ftc.teamcode.buttons;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.utils.Round;

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
            if (parent.autokey(spinner.up())) {
                spinner.increment();
            } else if (parent.autokey(spinner.down())) {
                spinner.decrement();
            }

            // Publish telemetry
            telemetry(spinner);
        }
    }

    public void telemetry(Spinner spinner) {
        String label = this.getClass().getSimpleName().substring(0, 4) +
                spinner.dataClass().getSimpleName().substring(0, 1) + ":" + spinner.name;

        // General case for most types
        String value = spinner.dataClass().cast(spinner.value).toString();
        // Special handling for doubles, to avoid runaway approximations
        switch (spinner.type) {
            case DOUBLE:
                value = ((Double) (Round.truncate((Double) spinner.value, 5))).toString();
                break;
        }
        robot.telemetry.addData(label, value);
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

    public float getFloat(String name) {
        return (float) getDouble(name);
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

    public void setLimit(String name, int limit, boolean max) {
        setLimit(name, (Integer) limit, max);
    }

    public void setLimit(String name, double limit, boolean max) {
        setLimit(name, (Double) limit, max);
    }

    private void setLimit(String name, Object lim, boolean max) {
        Spinner spinner = spinners.get(name);
        if (spinner == null) {
            return;
        }
        spinner.checkNullOrDataClass(lim, "limit");
        if (max) {
            spinner.max = lim;
        } else {
            spinner.min = lim;
        }
        spinner.value = spinner.limits(spinner.value);
    }

    // int data + increment
    public void add(String name,
                    Gamepad pad, PAD_BUTTON up, PAD_BUTTON down,
                    int increment, int value) {
        add(name, SPINNER_TYPE.INT, pad, up, down, increment, value);
    }

    // int data, String increment
    public void add(String name,
                    Gamepad pad, PAD_BUTTON up, PAD_BUTTON down,
                    String increment, int value) {
        add(name, SPINNER_TYPE.INT, pad, up, down, increment, value);
    }

    // double data + increment
    public void add(String name,
                    Gamepad pad, PAD_BUTTON up, PAD_BUTTON down,
                    double increment, double value) {
        add(name, SPINNER_TYPE.DOUBLE, pad, up, down, increment, value);
    }

    // double data, String increment
    public void add(String name,
                    Gamepad pad, PAD_BUTTON up, PAD_BUTTON down,
                    String increment, double value) {
        add(name, SPINNER_TYPE.DOUBLE, pad, up, down, increment, value);
    }

    protected void add(String name, SPINNER_TYPE type,
                       Gamepad pad, PAD_BUTTON up, PAD_BUTTON down,
                       Object increment, Object value) {
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
        Spinner spinner = new Spinner(this, name, type, increment, value);
        spinners.put(name, spinner);

        // Register buttons
        parent.register(spinner.up(), pad, up);
        parent.register(spinner.down(), pad, down);

        // Enable whenever something is added
        setEnable(true);
    }

    public void remove(String name) {
        Spinner spinner = spinners.get(name);
        if (spinner != null) {
            parent.deregister(spinner.up());
            parent.deregister(spinner.down());
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

        private final SpinnerHandler handler;
        private final String name;
        private final SPINNER_TYPE type;
        private final Object increment;
        private final boolean incrementIsName;

        private Object min;
        private Object max;
        private Object value;

        protected Spinner(SpinnerHandler handler, String name, SPINNER_TYPE type,
                          Object increment, Object value) {
            this.handler = handler;
            this.name = name;
            this.type = type;
            this.increment = increment;
            this.value = value;
            this.min = null;
            this.max = null;

            incrementIsName = String.class.isInstance(increment);
            if (!incrementIsName) {
                checkNullOrDataClass(increment, "increment");
            }
            checkNullOrDataClass(value, "value");
        }

        private void checkNullOrDataClass(Object obj, String name) {
            if (obj != null && !dataClass().isInstance(obj)) {
                throw new IllegalArgumentException(this.getClass().getSimpleName() +
                        ": Parameter '" + name + "' for type " + type +
                        " must be an instance of: " + dataClass().getName());
            }
        }

        protected Class dataClass() {
            Class cls = null;
            switch (type) {
                case INT:
                    cls = Integer.class;
                    break;
                case DOUBLE:
                    cls = Double.class;
                    break;
            }
            return cls;
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
                        val = Math.max((Integer) val, (Integer) min);
                        break;
                    case DOUBLE:
                        val = Math.max((Double) val, (Double) min);
                        break;
                }
            }
            if (max != null) {
                switch (type) {
                    case INT:
                        val = Math.min((Integer) val, (Integer) max);
                        break;
                    case DOUBLE:
                        val = Math.min((Double) val, (Double) max);
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
