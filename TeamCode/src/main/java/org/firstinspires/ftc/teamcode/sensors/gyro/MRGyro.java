package org.firstinspires.ftc.teamcode.sensors.gyro;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.utils.Heading;

public class MRGyro implements Gyro {
    private ModernRoboticsI2cGyro gyro;
    private boolean ready = false;
    private float offset = 0.0f;

    public MRGyro(HardwareMap map, Telemetry telemetry, String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": Null/empty name");
        }
        try {
            gyro = (ModernRoboticsI2cGyro) map.gyroSensor.get(name);
        } catch (Exception e) {
            gyro = null;
            telemetry.log().add(this.getClass().getSimpleName() + "No such device: " + name);
            return;
        }

        gyro.resetDeviceConfigurationForOpMode();
        gyro.calibrate();
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

    public void setOffset(float offset) {
        this.offset = offset;
    }

    public float getRaw() {
        if (!isReady()) {
            return 0.0f;
        }

        // Invert to make CW rotation increase the heading
        return -gyro.getIntegratedZValue();
    }

    public float getHeading() {
        return Heading.normalize(getRaw() + offset);
    }
}