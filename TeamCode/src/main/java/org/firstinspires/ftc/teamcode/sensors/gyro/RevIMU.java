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

class IMUWaiter implements Runnable {
    public static final int TIMEOUT = 1000;

    public static final String LOG_NAME = null;
    public static final String CALIBRATION_FILE = null;

    public static final int INTEGRATION_INTERVAL = 1000;
    public static final BNO055IMU.AccelerationIntegrator INTEGRATOR = new JustLoggingAccelerationIntegrator();

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
        imu.initialize(params);
        if (System.currentTimeMillis() - start > TIMEOUT) {
            telemetry.log().add(this.getClass().getName() + ": Failed to initialize");
            parent.setGyro(null);
            return;
        }

        // Start from 0, 0, 0, 0 if things look good
        imu.startAccelerationIntegration(new Position(), new Velocity(), INTEGRATION_INTERVAL);

        // Make the gyro available
        parent.setGyro(imu);
    }
}

public class RevIMU implements Gyro {
    private BNO055IMU gyro = null;
    private boolean ready = false;
    private int offset = 0;

    public RevIMU(HardwareMap map, Telemetry telemetry, String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(this.getClass().getName() + ": Null/empty name");
        }

        // Attempt to init
        BNO055IMU imu;
        try {
            imu = map.get(BNO055IMU.class, name);
        } catch (Exception e) {
            telemetry.log().add(this.getClass().getName() + "No such device: " + name);
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

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getRaw() {
        if (!isReady()) {
            return 0;
        }

        // Invert to make CW rotation increase with the heading
        return (int) -gyro.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;
    }

    public int getHeading() {
        return (getRaw() + offset);
    }
}
