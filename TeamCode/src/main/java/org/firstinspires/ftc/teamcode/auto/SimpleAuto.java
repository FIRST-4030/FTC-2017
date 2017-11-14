package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.config.WheelMotorConfigs;
import org.firstinspires.ftc.teamcode.driveto.DriveTo;
import org.firstinspires.ftc.teamcode.driveto.DriveToListener;
import org.firstinspires.ftc.teamcode.driveto.DriveToParams;
import org.firstinspires.ftc.teamcode.sensors.Gyro;
import org.firstinspires.ftc.teamcode.wheels.TankDrive;

import static org.firstinspires.ftc.teamcode.auto.DriveToMethods.*;


@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Simple Auto", group = "AutoTest")
public class SimpleAuto extends OpMode implements DriveToListener {

    // Devices and subsystems
    private TankDrive tank;
    private DriveTo drive;

    private Gyro gyro;

    @Override
    public void init() {

        // Placate drivers; sometimes VuforiaFTC is slow to init
        telemetry.addData(">", "Initializing...");
        telemetry.update();

        // Drive motors
        tank = new WheelMotorConfigs().init(hardwareMap, telemetry);
        tank.stop();

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
        telemetry.addData("Gyro Available", gyro.isAvailable());
        telemetry.addData("Gyro Ready", gyro.isReady());
        telemetry.addData("Heading", gyro.getHeading());
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
            drive = driveForward(this, tank, 254);
        }
    }

    @Override
    public void driveToStop(DriveToParams param) {
        DriveToMethods.stop(tank, param);
    }

    @Override
    public void driveToRun(DriveToParams param) {
        DriveToMethods.run(tank, param);
    }

    @Override
    public double driveToSensor(DriveToParams param) {
        return DriveToMethods.sensor(tank, param);
    }
}