package org.firstinspires.ftc.teamcode.sensors.switches;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Voltage implements Switch {
    private AnalogInput button;
    private float threshold = 0.5f;

    public Voltage(HardwareMap map, Telemetry telemetry, String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": Null/empty name");
        }
        try {
            button = map.analogInput.get(name);
        } catch (Exception e) {
            button = null;
            telemetry.log().add(this.getClass().getSimpleName() + "No such device: " + name);
            return;
        }
    }

    public boolean isAvailable() {
        return (button != null);
    }

    public void setThreshold(float volts) {
        threshold = volts;
    }

    public boolean get() {
        return isAvailable() && (button.getVoltage() < threshold);
    }
}
