package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.auto.CommonTasks;
import org.firstinspires.ftc.teamcode.auto.DriveToMethods;
import org.firstinspires.ftc.teamcode.config.MotorConfigs;
import org.firstinspires.ftc.teamcode.config.WheelMotorConfigs;
import org.firstinspires.ftc.teamcode.driveto.DriveTo;
import org.firstinspires.ftc.teamcode.driveto.DriveToListener;
import org.firstinspires.ftc.teamcode.driveto.DriveToParams;
import org.firstinspires.ftc.teamcode.sensors.Gyro;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;
import org.firstinspires.ftc.teamcode.wheels.TankDrive;

import static org.firstinspires.ftc.teamcode.auto.DriveToMethods.*;

@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Simple Auto", group = "Test")
public class SimpleAuto extends OpMode implements DriveToListener {

    // Devices and subsystems
    private CommonTasks common = null;
    private TankDrive tank;
    private DriveTo drive;
    private Motor lift;
    private Gyro gyro;

    // Lift zero testing
    enum LIFT_STATE implements OrderedEnum {
        TIMEOUT,
        INIT,
        RETRACT,
        READY,
        DONE;

        public LIFT_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public LIFT_STATE next() {
            return OrderedEnumHelper.next(this);
        }
    }

    private LIFT_STATE liftState = LIFT_STATE.INIT;
    private static final int LIFT_TIMEOUT = 1500;
    // In general you should init false, but for testing start with nothing
    private boolean liftReady = true;
    private boolean liftButton = false;
    private double liftTimeout = 0;

    @Override
    public void init() {

        // Placate drivers; sometimes VuforiaFTC is slow to init
        telemetry.addData(">", "Initializing...");
        telemetry.update();

        // Common init
        common = new CommonTasks(hardwareMap, telemetry);
        tank = common.initDrive();
        lift = common.initLift();

        // Gyro
        gyro = new Gyro(hardwareMap, "imu", telemetry);

        // Wait for the game to begin
        telemetry.addData(">", "Ready for game start");
        telemetry.update();
    }

    @Override
    public void init_loop() {
    }

    @Override
    public void start() {
        telemetry.clearAll();
    }

    @Override
    public void loop() {

        // Handle DriveTo driving
        if (drive != null) {
            // DriveTo
            drive.drive();

            // Return to teleop when complete
            if (drive.isDone()) {
                drive = null;
                tank.setTeleop(true);
            }
        }

        // Driver feedback
        telemetry.addData("Drive", drive);
        telemetry.addData("Heading", gyro.getHeading());
        telemetry.addData("Encoder", tank.getEncoder());
        telemetry.addData("Lift", lift.getEncoder());
        telemetry.addData("LiftZero", liftState);
        telemetry.addData("Gyro Ready", gyro.isReady());
        telemetry.addData("Time", (float) ((int) (time * 1000)) / 1000.0f);
        telemetry.addData("Lift Timeout", (float) ((int) (liftTimeout * 1000)) / 1000.0f);
        telemetry.update();

        /*
         * Cut the loop short when we are auto-driving
         * This keeps us out of the state machine until the last auto-drive command is complete
         */
        if (drive != null) {
            return;
        }

        // Test lift zero, with persistent timeout
        if (!liftReady) {
            switch (liftState) {
                case INIT:
                    liftTimeout = time + (LIFT_TIMEOUT / 1000);
                    liftState = liftState.next();
                    break;
                case RETRACT:
                    if (liftButton) {
                        liftState = liftState.next();
                    } else if (time > liftTimeout) {
                        lift.stop();
                        liftState = LIFT_STATE.TIMEOUT;
                    } else {
                        lift.setPower(LIFT_SPEED_DOWN);
                    }
                    break;
                case READY:
                    lift.stop();
                    lift.resetEncoder();
                    liftState = liftState.next();
                    break;
                case DONE:
                    liftReady = true;
                    break;
            }
        }

        if (gamepad1.a) {
            drive = driveForward(this, tank, 254);
        } else if (gamepad1.b) {
            liftReady = false;
            liftState = LIFT_STATE.INIT;
        } else if (gamepad1.dpad_left) {
            drive = turnDegrees(this, gyro, -30);
        } else if (gamepad1.dpad_right) {
            drive = turnDegrees(this, gyro, 30);
        }
    }

    @Override
    public void driveToStop(DriveToParams param) {
        DriveToMethods.stop(tank, param);
    }

    @Override
    public void driveToRun(DriveToParams param) {
        DriveToMethods.run(tank, gyro, param);
    }

    @Override
    public double driveToSensor(DriveToParams param) {
        return DriveToMethods.sensor(tank, param, gyro);
    }
}