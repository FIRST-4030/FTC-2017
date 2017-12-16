package org.firstinspires.ftc.teamcode.robot.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegistrar;

import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.robot.auto.CommonTasks;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;
import org.firstinspires.ftc.teamcode.utils.Round;

import static org.firstinspires.ftc.teamcode.robot.auto.CommonTasks.*;

@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Simple Auto", group = "Test")
public class SimpleAuto extends OpMode {

    // Devices and subsystems
    private Robot robot = null;
    private CommonTasks common = null;
    private AutoDriver driver = new AutoDriver();

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
    private double liftTimeout = 0;

    @Override
    public void init() {
        telemetry.addData(">", "Init…");
        telemetry.update();

        // Common init
        robot = new Robot(hardwareMap, telemetry);
        common = new CommonTasks(robot);
    }

    @Override
    public void init_loop() {
        telemetry.addData("Gyro", robot.gyro.isReady() ? "Ready" : "Calibrating…");
        if (robot.gyro.isReady()) {
            telemetry.addData(">", "Ready for game start");
        }
        telemetry.update();
    }

    @Override
    public void start() {
        telemetry.clearAll();
    }

    @Override
    public void loop() {

        // Handle AutoDriver driving
        if (driver.drive != null) {
            // DriveTo
            driver.drive.drive();

            // Return to teleop when complete
            if (driver.drive.isDone()) {
                driver.drive = null;
                robot.wheels.setTeleop(true);
            }
        }

        // Driver feedback
        telemetry.addData("LiftZero", liftState);
        telemetry.addData("Lift", robot.lift.getEncoder() + "/" + (robot.liftSwitch.get() ? "Down" : "Up"));
        telemetry.addData("Gyro", robot.gyro.isReady() ? robot.gyro.getHeading() : "<Calibrating>");
        telemetry.addData("Encoder", robot.wheels.getEncoder());
        telemetry.addData("Time/Drive", Round.truncate(time) + "/" + driver.drive);
        telemetry.update();

        /*
         * Cut the loop short when we are AutoDriver'ing
         * This keeps us out of the state machine until the preceding command is complete
         */
        if (driver.isRunning(time)) {
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
                    if (robot.liftSwitch.get()) {
                        liftState = liftState.next();
                    } else if (time > liftTimeout) {
                        robot.lift.stop();
                        liftState = LIFT_STATE.TIMEOUT;
                    } else {
                        robot.lift.setPower(LIFT_SPEED_DOWN);
                    }
                    break;
                case READY:
                    robot.lift.stop();
                    robot.lift.resetEncoder();
                    liftState = liftState.next();
                    break;
                case DONE:
                    liftReady = true;
                    break;
            }
        }

        if (gamepad1.a) {
            driver.drive = common.driveForward((int) (25.4 * 10));
        } else if (gamepad1.y) {
            driver.drive = common.driveForward((int) (25.4 * 20));
        } else if (gamepad1.b) {
            liftReady = false;
            liftState = LIFT_STATE.INIT;
        } else if (gamepad1.dpad_left) {
            driver.drive = common.turnToHeading(270);
        } else if (gamepad1.dpad_right) {
            driver.drive = common.turnToHeading(90);
        } else if(gamepad1.dpad_up){
            driver.drive = common.turnToHeading(0);
        } else if (gamepad1.dpad_down){
            driver.drive = common.turnToHeading(180);
        } else if (gamepad1.left_bumper){
            driver.drive = common.turnToHeading(315);
        } else if (gamepad1.right_bumper){
            driver.drive = common.turnToHeading(45);
        }
    }
}