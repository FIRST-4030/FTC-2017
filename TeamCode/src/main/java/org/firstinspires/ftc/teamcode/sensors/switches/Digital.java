package org.firstinspires.ftc.teamcode.sensors.switches;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Digital implements Switch {
    private DigitalChannel button;

    public Digital(HardwareMap map, Telemetry telemetry, String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": Null/empty name");
        }
        try {
            button = map.digitalChannel.get(name);
        } catch (Exception e) {
            button = null;
            telemetry.log().add(this.getClass().getSimpleName() + "No such device: " + name);
            return;
        }

        button.setMode(DigitalChannel.Mode.INPUT);
    }

    public boolean isAvailable() {
        return (button != null);
    }

    public boolean get() {
        return isAvailable() && button.getState();
    }
}
