package org.firstinspires.ftc.teamcode.robot.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.buttons.PAD_BUTTON;
import org.firstinspires.ftc.teamcode.buttons.BUTTON_TYPE;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.robot.CLAWS;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.robot.common.Common;
import org.firstinspires.ftc.teamcode.utils.Round;
import org.firstinspires.ftc.teamcode.vuforia.ImageFTC;
import org.firstinspires.ftc.teamcode.wheels.MOTOR_SIDE;

@TeleOp(name = "Calibration", group = "Test")
public class Calibration extends OpMode {

    private static final float SERVO_INTERVAL_INTERVAL = 0.01f;

    // Devices and subsystems
    private Robot robot = null;
    private Common common = null;
    private ButtonHandler buttons;

    // Run-time
    private int imageInterval = 10;
    private float servoInterval = 0.01f;
    private long imageTimestamp = 0;
    private final float[] rate = new float[MOTOR_SIDE.values().length];
    private final float[] min = new float[MOTOR_SIDE.values().length];
    private final float[] max = new float[MOTOR_SIDE.values().length];

    @Override
    public void init() {

        // Init the common tasks elements
        robot = new Robot(hardwareMap, telemetry);
        common = robot.common;

        // Put these servos someplace vaguely safe
        for (ServoFTC claw : robot.claws) {
            claw.setPositionRaw(0.5f);
        }
        robot.jewelArm.setPositionRaw(0.5f);

        // Start Vuforia tracking
        robot.vuforia.start();

        // Motor/Servo buttons
        buttons = new ButtonHandler(robot);
        buttons.register("CLAW-" + CLAWS.TOP + "-UP", gamepad1, PAD_BUTTON.dpad_up);
        buttons.register("CLAW-" + CLAWS.TOP + "-DOWN", gamepad1, PAD_BUTTON.dpad_down);
        buttons.register("CLAW-" + CLAWS.BOTTOM + "-UP", gamepad1, PAD_BUTTON.dpad_right);
        buttons.register("CLAW-" + CLAWS.BOTTOM + "-DOWN", gamepad1, PAD_BUTTON.dpad_left);
        buttons.register("ARM-UP", gamepad1, PAD_BUTTON.a);
        buttons.register("ARM-DOWN", gamepad1, PAD_BUTTON.b);
        buttons.register("SERVO-INTERVAL-UP", gamepad1, PAD_BUTTON.right_stick_button);
        buttons.register("SERVO-INTERVAL-DOWN", gamepad1, PAD_BUTTON.left_stick_button);

        // Image area buttons
        buttons.register("UL-INCREASE-X", gamepad2, PAD_BUTTON.dpad_right);
        buttons.register("UL-DECREASE-X", gamepad2, PAD_BUTTON.dpad_left);
        buttons.register("UL-INCREASE-Y", gamepad2, PAD_BUTTON.dpad_down);
        buttons.register("UL-DECREASE-Y", gamepad2, PAD_BUTTON.dpad_up);
        buttons.register("LR-INCREASE-X", gamepad2, PAD_BUTTON.b);
        buttons.register("LR-DECREASE-X", gamepad2, PAD_BUTTON.x);
        buttons.register("LR-INCREASE-Y", gamepad2, PAD_BUTTON.a);
        buttons.register("LR-DECREASE-Y", gamepad2, PAD_BUTTON.y);
        buttons.register("AREA-INTERVAL-UP", gamepad2, PAD_BUTTON.right_stick_button);
        buttons.register("AREA-INTERVAL-DOWN", gamepad2, PAD_BUTTON.left_stick_button);
        buttons.register("CAPTURE", gamepad2, PAD_BUTTON.left_bumper, BUTTON_TYPE.TOGGLE);
        buttons.register("SAVE", gamepad2, PAD_BUTTON.right_bumper);
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
        robot.lift.resetEncoder();
        robot.wheels.resetEncoder();
    }

    @Override
    public void loop() {

        // Update buttons
        buttons.update();
        jewelAreaInput();

        // Adjust the lift and wheels
        robot.wheels.setPowerRaw(-gamepad1.left_stick_y);
        robot.lift.setPower(-gamepad1.right_stick_y);

        // Adjust the claws
        for (CLAWS claw : CLAWS.values()) {
            if (buttons.get("CLAW-" + claw + "-UP")) {
                robot.claws[claw.ordinal()].setPositionRaw(robot.claws[claw.ordinal()].getPostion() + servoInterval);
            } else if (buttons.get("CLAW-" + claw + "-DOWN")) {
                robot.claws[claw.ordinal()].setPositionRaw(robot.claws[claw.ordinal()].getPostion() - servoInterval);
            }
        }

        // Adjust the jewel arm
        if (buttons.get("ARM-UP")) {
            robot.jewelArm.setPositionRaw(robot.jewelArm.getPostion() + servoInterval);
        } else if (buttons.get("ARM-DOWN")) {
            robot.jewelArm.setPositionRaw(robot.jewelArm.getPostion() - servoInterval);
        }

        // Adjust the servo adjustment rate
        if (buttons.get("SERVO-INTERVAL-UP")) {
            servoInterval += SERVO_INTERVAL_INTERVAL;
        }
        if (buttons.get("SERVO-INTERVAL-DOWN")) {
            servoInterval -= SERVO_INTERVAL_INTERVAL;
        }

        // Mirror changes in our capture mode to vuforia
        if (robot.vuforia.capturing() != buttons.get("CAPTURE")) {
            robot.vuforia.enableCapture(buttons.get("CAPTURE"));
        }
        // Capture when enabled
        if (buttons.get("CAPTURE")) {
            robot.vuforia.capture();
        }
        // Process the capture if we've got one
        ImageFTC image = robot.vuforia.getImage();
        if (image != null && image.getTimestamp() != imageTimestamp) {
            imageTimestamp = image.getTimestamp();
            common.jewel.setImage(image);
            if (buttons.held("SAVE")) {
                image.savePNG("calibration-" + image.getTimestamp() + ".png");
            }
        }

        // Wheel rates
        for (MOTOR_SIDE side : MOTOR_SIDE.values()) {
            rate[side.ordinal()] = robot.wheels.getRate(side);
            max[side.ordinal()] = Math.max(rate[side.ordinal()], max[side.ordinal()]);
            min[side.ordinal()] = Math.min(rate[side.ordinal()], min[side.ordinal()]);
        }

        // Feedback
        for (CLAWS claw : CLAWS.values()) {
            telemetry.addData("Claw " + claw, Round.truncate(robot.claws[claw.ordinal()].getPostion()));
        }
        telemetry.addData("Servo Interval", Round.truncate(servoInterval));
        telemetry.addData("Lift", robot.lift.getEncoder());
        telemetry.addData("Lift Switch", robot.liftSwitch.get());
        telemetry.addData("Wheels L/R", robot.wheels.getEncoder(MOTOR_SIDE.LEFT) +
                "/" + robot.wheels.getEncoder(MOTOR_SIDE.RIGHT));
        telemetry.addData("Wheels Rate", Round.truncate(robot.wheels.getRate()));
        telemetry.addData("Left Rate (Min/Max)", Round.truncate(rate[MOTOR_SIDE.LEFT.ordinal()]) +
                "\t(" + Round.truncate(min[MOTOR_SIDE.LEFT.ordinal()]) + "/" + Round.truncate(max[MOTOR_SIDE.LEFT.ordinal()]) + ")");
        telemetry.addData("Right Rate (Min/Max)", Round.truncate(rate[MOTOR_SIDE.RIGHT.ordinal()]) +
                "\t(" + Round.truncate(min[MOTOR_SIDE.RIGHT.ordinal()]) + "/" + Round.truncate(max[MOTOR_SIDE.RIGHT.ordinal()]) + ")");
        telemetry.addData("Arm", Round.truncate(robot.jewelArm.getPostion()));
        telemetry.addData("Gyro", robot.gyro.isReady() ? Round.truncate(robot.gyro.getHeading()) : "<Not Ready>");
        telemetry.addData("", "");
        telemetry.addData("Image", image != null ? imageText(image) : "<No image>");
        telemetry.addData("Red Side", image != null ? (common.jewel.isLeftRed() ? "Left" : "Right") : "<No image>");
        telemetry.addData("Image Interval", imageInterval);
        telemetry.addData("Image UL", common.jewel.UL[0] + ", " + common.jewel.UL[1]);
        telemetry.addData("Image LR", common.jewel.LR[0] + ", " + common.jewel.LR[1]);
        telemetry.addData("", "");
        telemetry.update();
    }

    private String imageText(ImageFTC image) {
        return "(" + image.getWidth() + "," + image.getHeight() + ") " + image.getTimestamp();
    }

    private void jewelAreaInput() {
        if (buttons.get("UL-INCREASE-X")) {
            common.jewel.changeArea(false, true, imageInterval);
        } else if (buttons.get("UL-DECREASE-X")) {
            common.jewel.changeArea(false, true, -imageInterval);
        }
        if (buttons.get("UL-INCREASE-Y")) {
            common.jewel.changeArea(false, false, imageInterval);
        } else if (buttons.get("UL-DECREASE-Y")) {
            common.jewel.changeArea(false, false, -imageInterval);
        }

        if (buttons.get("LR-INCREASE-X")) {
            common.jewel.changeArea(true, true, imageInterval);
        } else if (buttons.get("LR-DECREASE-X")) {
            common.jewel.changeArea(true, true, -imageInterval);
        }
        if (buttons.get("LR-INCREASE-Y")) {
            common.jewel.changeArea(true, false, imageInterval);
        } else if (buttons.get("LR-DECREASE-Y")) {
            common.jewel.changeArea(true, false, -imageInterval);
        }

        if (buttons.get("AREA-INTERVAL-UP")) {
            imageInterval++;
        } else if (buttons.get("AREA-INTERVAL-DOWN")) {
            imageInterval = Math.max(1, imageInterval - 1);
        }
    }
}
