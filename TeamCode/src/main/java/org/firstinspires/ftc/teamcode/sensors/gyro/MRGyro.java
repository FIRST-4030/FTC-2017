package org.firstinspires.ftc.teamcode.sensors.gyro;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class MRGyro implements Gyro {
    private ModernRoboticsI2cGyro gyro;
    private boolean ready = false;
    private int offset = 0;
    private boolean started = false;

    public MRGyro(HardwareMap map, Telemetry telemetry, String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(this.getClass().getName() + ": Null/empty name");
        }
        try {
            gyro = (ModernRoboticsI2cGyro) map.gyroSensor.get(name);
        } catch (Exception e) {
            gyro = null;
            telemetry.log().add(this.getClass().getName() + "No such device: " + name);
            return;
        }
    }

    public void start() {
        if (!isAvailable() || started) {
            return;
        }
        gyro.resetDeviceConfigurationForOpMode();
        gyro.calibrate();
        started = true;
    }

    public boolean isAvailable() {
        return gyro != null;
    }

    public boolean isReady() {
        if (!ready && isAvailable() && !gyro.isCalibrating()) {
            ready = true;
        }
        return ready;
    }

    public void reset() {
        if (!isAvailable()) {
            return;
        }
        gyro.resetZAxisIntegrator();
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getRaw() {
        if (!isReady()) {
            return 0;
        }

        // Invert to make CW rotation increase the heading
        return -gyro.getIntegratedZValue();
    }

    public int getHeading() {
        return (getRaw() + offset);
    }
}