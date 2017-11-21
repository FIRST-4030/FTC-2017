package org.firstinspires.ftc.teamcode.sensors;

import com.qualcomm.hardware.bosch.BNO055IMU;
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

    public Gyro(HardwareMap map, Telemetry telemetry, String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(this.getClass().getName() + ": Null/empty name");
        }
        try {
            gyro = map.get(BNO055IMU.class, name);
        } catch (Exception e) {
            gyro = null;
            telemetry.log().add(this.getClass().getName() + "No such device: " + name);
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
        heading = normalizeHeading(heading);
        int offset = (heading - getRawHeadingNormalized()) % FULL_CIRCLE;
        if (offset > FULL_CIRCLE / 2) {
            offset -= FULL_CIRCLE;
        }
        setOffset(offset);
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getRawHeading() {
        if (!isReady()) {
            return 0;
        }

        // Invert to make CW rotation increase with the heading
        return (int) -gyro.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;
    }

    public int getHeading() {
        return (getRawHeading() + offset);
    }

    private int getHeadingNormalized() {
        return normalizeHeading(getHeading());
    }

    private int getRawHeadingNormalized() {
        return normalizeHeading(getRawHeading());
    }

    /**
     * @param heading Any heading
     * @return The same heading projected into the space between 0 and 359, inclusively
     */
    public static int normalizeHeading(int heading) {
        return ((heading % FULL_CIRCLE) + FULL_CIRCLE) % FULL_CIRCLE;
    }
}