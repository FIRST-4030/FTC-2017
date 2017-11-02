package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.config.WheelMotorConfigs;
import org.firstinspires.ftc.teamcode.driveto.DriveTo;
import org.firstinspires.ftc.teamcode.driveto.DriveToListener;
import org.firstinspires.ftc.teamcode.driveto.DriveToParams;
import org.firstinspires.ftc.teamcode.wheels.TankDrive;

@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Simple Auto", group = "AutoTest")
public class SimpleAuto extends OpMode implements DriveToListener {

    // Devices and subsystems
    private TankDrive tank;
    private DriveTo drive;

    @Override
    public void init() {

        // Placate drivers; sometimes VuforiaFTC is slow to init
        telemetry.addData(">", "Initializing...");
        telemetry.update();

        // Drive motors
        tank = new WheelMotorConfigs().init(hardwareMap, telemetry);
        tank.stop();

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
        telemetry.addData("Encoder", tank.getEncoder());
        telemetry.update();

        /*
         * Cut the loop short when we are auto-driving
         * This keeps us out of the state machine until the last auto-drive command is complete
         */
        if (drive != null) {
            return;
        }

        if (gamepad1.a) {
            drive = DriveToMethods.driveForward(this, tank, 254);
        }
    }

    @Override
    public void driveToStop(DriveToParams param) {
        switch ((DriveToMethods.SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
                tank.stop();
                break;
        }
    }

    @Override
    public void driveToRun(DriveToParams param) {
        // Remember that "forward" is "negative" per the joystick conventions
        switch ((DriveToMethods.SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
                tank.setSpeed(DriveToMethods.SPEED_FORWARD);
                break;
        }
    }

    @Override
    public double driveToSensor(DriveToParams param) {
        double value = 0;
        switch ((DriveToMethods.SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
                value = tank.getEncoder();
                break;
        }
        return value;
    }
}