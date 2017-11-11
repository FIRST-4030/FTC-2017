package org.firstinspires.ftc.teamcode.sensors;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.BNO055IMUImpl;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

public class Gyro {
    private static final int FULL_CIRCLE = 360;
    private static final String LOG_NAME = null;
    private static final String CALIBRATION_FILE = null;

    private static final int INTEGRATION_INTERVAL = 1000;
    private static final BNO055IMU.AccelerationIntegrator INTEGRATOR = new JustLoggingAccelerationIntegrator();

    private BNO055IMU gyro = null;
    private boolean ready = false;
    private int offset = 0;

    public Gyro(HardwareMap map, String name, Telemetry telemetry) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(this.getClass().getName() + ": Null/empty name");
        }
        try {
            gyro = (BNO055IMU) map.gyroSensor.get(name);
        } catch (Exception e) {
            gyro = null;
            if (telemetry != null) {
                telemetry.log().add(this.getClass().getName() + "No such device: " + name);
            }
            return;
        }

        // Basic parameters for the IMU, so we get units we like and whatnot
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        params.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;

        // Optionally select an integration algorithm
        if (INTEGRATOR != null) {
            params.accelerationIntegrationAlgorithm = INTEGRATOR;
        }

        // Optionally log to the phone's catlog
        if (LOG_NAME != null && !LOG_NAME.isEmpty()) {
            params.loggingEnabled = true;
            params.loggingTag = LOG_NAME;
        }

        // Optionally load static calibration data
        if (CALIBRATION_FILE != null && !CALIBRATION_FILE.isEmpty()) {
            params.calibrationDataFile = CALIBRATION_FILE;
        }
        gyro.initialize(params);

        // Start the IMU
        gyro.startAccelerationIntegration(new Position(), new Velocity(), INTEGRATION_INTERVAL);
    }

    public void disable() {
        ready = false;
        gyro = null;
    }

    public boolean isAvailable() {
        return gyro != null;
    }

    public boolean isReady() {
        if (!ready && isAvailable() && gyro.isGyroCalibrated()) {
            ready = true;
        }
        return ready;
    }

    public void setHeading(int heading) {
        // Normalize heading and offset
        heading = (heading + FULL_CIRCLE) % FULL_CIRCLE;
        int offset = (heading - getHeadingBasic(true)) % FULL_CIRCLE;
        if (offset > FULL_CIRCLE / 2) {
            offset -= FULL_CIRCLE;
        }
        setOffset(offset);
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getHeadingRaw() {
        if (!isReady()) {
            return 0;
        }

        // Invert to make CW rotation increase the heading
        return (int) -gyro.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;
    }

    public int getHeading() {
        return (getHeadingRaw() + offset);
    }

    public int getHeadingBasic() {
        return getHeadingBasic(false);
    }

    private int getHeadingBasic(boolean raw) {
        int heading;
        if (raw) {
            heading = getHeadingRaw();
        } else {
            heading = getHeading();
        }
        return ((heading % FULL_CIRCLE) + FULL_CIRCLE) % FULL_CIRCLE;
    }
}