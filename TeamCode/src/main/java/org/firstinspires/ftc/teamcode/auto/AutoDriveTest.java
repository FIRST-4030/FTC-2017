package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import org.firstinspires.ftc.teamcode.driveto.*;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * Created by alexw on 10/27/2017.
 */

public class AutoDriveTest extends OpMode implements DriveToListener{

    //starting values
    private static final float ENCODER_PER_MM = 3.2f;
    private static final int OVERRUN_ENCODER = 25;
    private static final int DRIVE_DISTANCE = 1830;
    private DriveTo drive;

    //motor objects
    public DcMotor L;
    public DcMotor R;


    //initialization loop, gets motors from hardware map
    public void init()
    {
        L =  hardwareMap.dcMotor.get("L");
        R =  hardwareMap.dcMotor.get("R");
    }

    @Override
    public void init_loop()
    {}

    @Override
    public void start()
    {
        telemetry.clearAll();

    }

    @Override
    public void loop()
    {
        driveForward(DRIVE_DISTANCE);
    }

    public void driveToStop(DriveToParams params)
    {}

    public void driveToRun(DriveToParams params)
    {}

    enum SENSOR_TYPE {
        GYRO, DRIVE_ENCODER, SHOOT_ENCODER
    }

    public double driveToSensor(DriveToParams params)
    {
        double value = 0;
        switch ((SENSOR_TYPE) param.reference) {
            case GYRO:
                value = gyro.getHeading();
                break;
            case DRIVE_ENCODER:
                value = tank.getEncoder(ENCODER_INDEX);
                break;
            case SHOOT_ENCODER:
                value = shooter.getEncoder();
                break;
        }
        return value;
    }

    private void driveForward(int distance) {
        //tank.setTeleop(false);
        /*DriveToParams param = new DriveToParams(this, SENSOR_TYPE.DRIVE_ENCODER);
        int ticks = (int) ((float) -distance * ENCODER_PER_MM);
        //param.lessThan(ticks + tank.getEncoder(ENCODER_INDEX) - OVERRUN_ENCODER);
        param.lessThan(ticks + L.getCurrentPosition() - OVERRUN_ENCODER);
        drive = new DriveTo(new DriveToParams[]{param});*/

        int ticks = (int) ((float) distance * ENCODER_PER_MM);
        L.setPower(.5);
        R.setPower(.5);
        L.setTargetPosition(ticks);
        R.setTargetPosition(ticks);
    }
}
