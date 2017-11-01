package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.WestCoastOpMode;
import org.firstinspires.ftc.teamcode.buttons.SinglePressButton;
import org.firstinspires.ftc.teamcode.config.WheelMotorConfigs;
import org.firstinspires.ftc.teamcode.driveto.DriveTo;
import org.firstinspires.ftc.teamcode.driveto.DriveToListener;
import org.firstinspires.ftc.teamcode.driveto.DriveToParams;
import org.firstinspires.ftc.teamcode.wheels.TankDrive;

import java.util.NoSuchElementException;

@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Straight Line", group = "Auto")
public class StraightLine extends WestCoastOpMode implements DriveToListener {

    // Driving constants
    private static final float ENCODER_PER_MM = 3.2f;
    private static final float SPEED_DRIVE = 1.0f;
    private static final int OVERRUN_ENCODER = 10;


    // Devices and subsystems
    private TankDrive tank = null;
    private DriveTo drive = null;

    // Runtime state
    private AUTO_STATE state = AUTO_STATE.INIT;
    private double timer = 0;

    // Init-time config
    private SinglePressButton up;
    private SinglePressButton down;
    private SinglePressButton left;
    private SinglePressButton right;
    private DISTANCE distance = DISTANCE.SHORT;
    private DELAY delay = DELAY.NONE;

    // Sensor reference types for our DriveTo callbacks
    enum SENSOR_TYPE {
        DRIVE_ENCODER
    }

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

        // Update the dpad buttons
        up.update(gamepad1.dpad_up);
        down.update(gamepad1.dpad_down);
        left.update(gamepad1.dpad_left);
        right.update(gamepad1.dpad_right);

        // Adjust delay
        if (up.active()) {
            delay = delay.next();
        } else if (down.active()) {
            delay = delay.prev();
        }

        // Adjust distance
        if (right.active()) {
            distance = distance.next();
        } else if (left.active()) {
            distance = distance.prev();
        }

        // Driver feedback
        telemetry.addData("Delay", delay);
        telemetry.addData("Distance", distance);
        telemetry.update();
    }

    @Override
    public void start() {
        telemetry.clearAll();

        // Steady...
        state = AUTO_STATE.first;
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
         * Cut the loop short when we are auto-driving or waiting on a timer
         * This keeps us out of the state machine until the preceding auto-drive command is complete
         */
        if (drive != null || timer > time) {
            return;
        }

        // Main state machine
        switch (state) {
            case INIT:
                state = state.next();
                break;
            case DELAY:
                timer = time + delay.seconds();
                state = state.next();
                break;
            case LIFTSTUFF:
                setServoPosition(TOP_CLAW, UPPER_CLAW_MAX);
                setServoPosition(BOTTOM_CLAW, LOWER_CLAW_MAX);
                setLiftPower(lift, -1);
                timer = time + 1;
                state = state.next();
                break;
            case DRIVE_FORWARD:
                driveForward(distance.millimeters());
                setLiftPower(lift, 0);
                state = state.next();
                break;
            case DONE:
                // Nothing
                break;
        }
    }

    @Override
    public void driveToStop(DriveToParams param) {
        switch ((SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
                tank.stop();
                break;
        }
    }

    @Override
    public void driveToRun(DriveToParams param) {
        // Remember that "forward" is "negative" per the joystick conventions
        switch ((SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
                tank.setSpeed(-SPEED_DRIVE);
                break;
        }
    }

    @Override
    public double driveToSensor(DriveToParams param) {
        double value = 0;
        switch ((SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
                value = tank.getEncoder();
                break;
        }
        return value;
    }

    private void driveForward(int distance) {
        tank.setTeleop(false);
        DriveToParams param = new DriveToParams(this, SENSOR_TYPE.DRIVE_ENCODER);
        int ticks = (int) ((float) -distance * ENCODER_PER_MM);
        param.lessThan(ticks + tank.getEncoder() - OVERRUN_ENCODER);
        drive = new DriveTo(new DriveToParams[]{param});
    }

    // Define the order of auto routine components
    enum AUTO_STATE {
        INIT,
        DELAY,
        LIFTSTUFF,
        DRIVE_FORWARD,
        DONE;

        // Private static copy to avoid repeated calls to values()
        private static final AUTO_STATE[] values = values();

        public AUTO_STATE prev() {
            int i = ordinal() - 1;
            if (i < 0) {
                throw new NoSuchElementException();
            }
            return values[i];
        }

        public AUTO_STATE next() {
            int i = ordinal() + 1;
            if (i >= values.length) {
                throw new NoSuchElementException();
            }
            return values[i];
        }

        public static final AUTO_STATE first = INIT;
        public static final AUTO_STATE last = DONE;
    }

    // Configurable straight-line distance
    enum DISTANCE {
        SHORT(965),
        LONG(1016);

        private int millimeters;

        DISTANCE(int millimeters) {
            this.millimeters = millimeters;
        }

        public int millimeters() {
            return millimeters;
        }

        // Bounded prev/next methods
        public DISTANCE prev() {
            int i = ordinal() + 1;
            if (i < 0) {
                i = 0;
            }
            return values()[i];
        }

        public DISTANCE next() {
            int i = ordinal() + 1;
            if (i >= values().length) {
                i = values().length - 1;
            }
            return values()[i];
        }
    }

    // Configurable delay
    enum DELAY {
        NONE(0),
        LONG(3000);

        private int milliseconds;

        DELAY(int milliseconds) {
            this.milliseconds = milliseconds;
        }

        public int milliseconds() {
            return milliseconds;
        }

        public double seconds() {
            return milliseconds / 1000.0d;
        }

        // Bounded prev/next methods
        public DELAY prev() {
            int i = ordinal() + 1;
            if (i < 0) {
                i = 0;
            }
            return values()[i];
        }

        public DELAY next() {
            int i = ordinal() + 1;
            if (i >= values().length) {
                i = values().length - 1;
            }
            return values()[i];
        }
    }
}