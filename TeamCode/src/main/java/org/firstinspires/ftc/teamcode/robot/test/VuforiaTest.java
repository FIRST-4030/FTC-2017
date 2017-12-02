package org.firstinspires.ftc.teamcode.robot.test;

import android.graphics.Color;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.teamcode.buttons.BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.field.VuforiaConfigs;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.robot.auto.CommonTasks;
import org.firstinspires.ftc.teamcode.vuforia.ImageFTC;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "Vuforia Test", group = "Test")
public class VuforiaTest extends OpMode {
    private final static int COLOR_SLICES = 5;

    // Devices and subsystems
    private Robot robot = null;
    private CommonTasks common = null;

    private ButtonHandler buttons = new ButtonHandler();

    // Dynamic things we need to remember
    private int lastBearing = 0;
    private int lastDistance = 0;
    private String lastTarget = "<None>";
    private RelicRecoveryVuMark lastMark = RelicRecoveryVuMark.UNKNOWN;
    private String lastImage = "<None>";
    private String lastRGB = "<None>";

    private int cornerIntervalInterval = 1;

    @Override
    public void init() {

        // Init the robot
        robot = new Robot(hardwareMap, telemetry);
        common = new CommonTasks(robot);

        // upper left jewel boundary
        buttons.register("UL-INCREASE-X", gamepad1, BUTTON.dpad_right);
        buttons.register("UL-DECREASE-X", gamepad1, BUTTON.dpad_left);
        buttons.register("UL-INCREASE-Y", gamepad1, BUTTON.dpad_down);
        buttons.register("UL-DECREASE-Y", gamepad1, BUTTON.dpad_up);
        // bottom right jewel boundary
        buttons.register("LR-INCREASE-X", gamepad1, BUTTON.b);
        buttons.register("LR-DECREASE-X", gamepad1, BUTTON.x);
        buttons.register("LR-INCREASE-Y", gamepad1, BUTTON.a);
        buttons.register("LR-DECREASE-Y", gamepad1, BUTTON.y);
        // change rate
        buttons.register("INCREASE-CHANGE-RATE", gamepad1, BUTTON.right_stick_button);
        buttons.register("DECREASE-CHANGE-RATE", gamepad1, BUTTON.left_stick_button);


        // Wait for the game to begin
        telemetry.addData(">", "Ready for game start");
        telemetry.update();
    }

    @Override
    public void start() {
        telemetry.clearAll();

        // Start Vuforia tracking and capture
        robot.vuforia.start();
        robot.vuforia.enableCapture(true);
    }

    @Override
    public void loop() {

        // Update our location and target info
        robot.vuforia.track();

        // Collect data about the first visible target
        String target = null;
        int bearing = 0;
        int distance = 0;
        if (!robot.vuforia.isStale()) {
            for (String t : robot.vuforia.getVisible().keySet()) {
                if (robot.vuforia.getVisible(t)) {
                    target = t;
                    int index = robot.vuforia.getTargetIndex(t);
                    bearing = robot.vuforia.bearing(index);
                    distance = robot.vuforia.distance(index);
                    break;
                }
            }
            lastTarget = target;
            lastBearing = bearing;
            lastDistance = distance;
        }

        // Read the VuMark
        lastMark = RelicRecoveryVuMark.from(robot.vuforia.getTrackable(VuforiaConfigs.TargetNames[0]));

        // Grab and optionally save an image
        robot.vuforia.capture();
        ImageFTC image = robot.vuforia.getImage();
        if (image != null && gamepad1.a) {
            image.savePNG("vuforia-" + image.getTimestamp() + ".png");
        }

        // RGB analysis of each of COLOR_SLICES number of equal slices of the image
        // image.rgb() takes an upper-left and lower-right pixel location and returns the
        // average color of all pixels in that rectangle.
        if (image != null) {
            lastImage = "(" + image.getWidth() + "," + image.getHeight() + ") " + image.getTimestamp();
            int slice = image.getWidth() / COLOR_SLICES;
            StringBuilder lastRGBStr = new StringBuilder();
            for (int i = 0; i < COLOR_SLICES; i++) {
                int rgb = image.rgb(
                        new int[]{(i * slice), 0},
                        new int[]{((i + 1) * slice) - 1, image.getHeight() - 1});
                lastRGBStr.append(" (").append(Color.red(rgb)).append(",").append(Color.green(rgb)).append(",").append(Color.blue(rgb)).append(")");
            }
            lastRGB = lastRGBStr.toString();
        }

        handleInput();

        // Driver feedback
        robot.vuforia.display(telemetry);
        telemetry.addData("Mark", lastMark);
        telemetry.addData("RGB", lastRGB);
        telemetry.addData("Target (" + lastTarget + ")", lastDistance + "mm @ " + lastBearing + "°");
        telemetry.addData("Image", lastImage);
        telemetry.addData("Interval Interval", cornerIntervalInterval);
        telemetry.addData("ULX", common.jewelUL[0]);
        telemetry.addData("ULY", common.jewelUL[1]);
        telemetry.addData("LRX", common.jewelLR[0]);
        telemetry.addData("LRY", common.jewelLR[1]);
        if(image != null) telemetry.addData("Left", common.leftJewelRed(image) ? "Red" : "Blue");
        telemetry.addData("", "");
        telemetry.update();

    }

    private void handleInput(){

        if(buttons.get("UL-INCREASE-X")) common.setJewelUL(new int[]{common.jewelUL[0] + cornerIntervalInterval, common.jewelUL[0]});
        if(buttons.get("UL-DECREASE-X")) common.setJewelUL(new int[]{common.jewelUL[0] - cornerIntervalInterval, common.jewelUL[0]});
        if(buttons.get("UL-INCREASE-Y")) common.setJewelUL(new int[]{common.jewelUL[1], common.jewelUL[1] + cornerIntervalInterval});
        if(buttons.get("UL-DECREASE-Y")) common.setJewelUL(new int[]{common.jewelUL[1], common.jewelUL[1] - cornerIntervalInterval});

        if(buttons.get("LR-INCREASE-X")) common.setJewelLR(new int[]{common.jewelLR[0] + cornerIntervalInterval, common.jewelLR[0]});
        if(buttons.get("LR-DECREASE-X")) common.setJewelLR(new int[]{common.jewelLR[0] - cornerIntervalInterval, common.jewelLR[0]});
        if(buttons.get("LR-INCREASE-Y")) common.setJewelLR(new int[]{common.jewelLR[1], common.jewelLR[1] + cornerIntervalInterval});
        if(buttons.get("LR-DECREASE-Y")) common.setJewelLR(new int[]{common.jewelLR[1], common.jewelLR[1] - cornerIntervalInterval});

        if(buttons.get("INCREASE-CHANGE-RATE")) cornerIntervalInterval += 1;
        if(buttons.get("DECREASE-CHANGE-RATE")) cornerIntervalInterval = Math.max(1, cornerIntervalInterval);

    }

}