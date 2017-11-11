package org.firstinspires.ftc.teamcode.sensors;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

public class Gyro {
    private static final int FULL_CIRCLE = 360;

    private BNO055IMU gyro;
    private boolean ready = false;
    private int offset = 0;

    public Gyro(HardwareMap map, String name) {
        ready = false;
        offset = 0;
        try {
            gyro = (BNO055IMU) map.gyroSensor.get(name);
            BNO055IMU.Parameters params = new BNO055IMU.Parameters();
            params.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
            params.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
            //params.calibrationDataFile = "BNO055IMUCalibration.json";
            params.loggingEnabled      = false;
            gyro.initialize(params);
        } catch (Exception e) {
            gyro = null;
        }
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
        return (int)-gyro.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;
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