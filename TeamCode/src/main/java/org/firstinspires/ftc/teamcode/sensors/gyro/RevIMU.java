package org.firstinspires.ftc.teamcode.sensors.gyro;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;
import org.firstinspires.ftc.teamcode.utils.Heading;

class IMUWaiter implements Runnable {
    private static final int TIMEOUT = 2500;

    private static final String LOG_NAME = null;
    private static final String CALIBRATION_FILE = null;

    private static final int INTEGRATION_INTERVAL = 1000;
    private static final BNO055IMU.AccelerationIntegrator INTEGRATOR = new JustLoggingAccelerationIntegrator();

    private final BNO055IMU imu;
    private final RevIMU parent;
    private final Telemetry telemetry;

    public IMUWaiter(RevIMU parent, BNO055IMU imu, Telemetry telemetry) {
        this.parent = parent;
        this.imu = imu;
        this.telemetry = telemetry;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void run() {
        // Record the start time so we can detect TIMEOUTs
        long start = System.currentTimeMillis();

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

        // Init -- this is where we hang
        try {
            imu.initialize(params);
        } catch (Exception e) {
            fail();
            return;
        }
        if (System.currentTimeMillis() - start > TIMEOUT) {
            fail();
            return;
        }

        // Start from 0, 0, 0, 0 if things look good
        imu.startAccelerationIntegration(new Position(), new Velocity(), INTEGRATION_INTERVAL);

        // Make the gyro available
        parent.setGyro(imu);
    }

    private void fail() {
        telemetry.log().add(this.getClass().getSimpleName() + ": Failed to initialize");
        parent.setGyro(null);
    }
}

public class RevIMU implements Gyro {
    private BNO055IMU gyro = null;
    private boolean ready = false;
    private float offset = 0.0f;

    public RevIMU(HardwareMap map, Telemetry telemetry, String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": Null/empty name");
        }

        // Attempt to init
        BNO055IMU imu;
        try {
            imu = map.get(BNO055IMU.class, name);
        } catch (Exception e) {
            telemetry.log().add(this.getClass().getSimpleName() + "No such device: " + name);
            return;
        }

        // Start the IMU in a background thread -- it behaves poorly when not available
        IMUWaiter waiter = new IMUWaiter(this, imu, telemetry);
        Thread thread = new Thread(waiter);
        thread.start();
    }

    protected void setGyro(BNO055IMU gyro) {
        this.gyro = gyro;
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

    public void setOffset(float offset) {
        this.offset = offset;
    }

    public float getRaw() {
        if (!isReady()) {
            return 0.0f;
        }

        // Invert to make CW rotation increase with the heading
        return -gyro.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;
    }

    public float getHeading() {
        return Heading.normalize(getRaw() + offset);
    }
}
