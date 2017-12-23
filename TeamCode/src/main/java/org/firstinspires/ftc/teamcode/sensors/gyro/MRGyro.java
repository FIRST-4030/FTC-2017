package org.firstinspires.ftc.teamcode.sensors.gyro;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.utils.Heading;

public class MRGyro implements Gyro {
    private ModernRoboticsI2cGyro gyro;
    private boolean ready = false;
    private double offset = 0.0d;

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

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public double getRaw() {
        if (!isReady()) {
            return 0.0d;
        }

        // Invert to make CW rotation increase the heading
        return -gyro.getIntegratedZValue();
    }

    public double getHeading() {
        return Heading.normalize(getRaw() + offset);
    }
}