package org.firstinspires.ftc.teamcode.robot.test;

import android.graphics.Color;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
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

    // Dynamic things we need to remember
    private int lastBearing = 0;
    private int lastDistance = 0;
    private String lastTarget = "<None>";
    private RelicRecoveryVuMark lastMark = RelicRecoveryVuMark.UNKNOWN;
    private String lastImage = "<None>";
    private String lastRGB = "<None>";

    @Override
    public void init() {

        // Init the robot
        robot = new Robot(hardwareMap, telemetry);
        common = new CommonTasks(robot);

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

        // Driver feedback
        robot.vuforia.display(telemetry);
        telemetry.addData("Mark", lastMark);
        telemetry.addData("RGB", lastRGB);
        telemetry.addData("Target (" + lastTarget + ")", lastDistance + "mm @ " + lastBearing + "°");
        telemetry.addData("Image", lastImage);
        telemetry.addData("Left", common.leftJewelRed() ? "Red" : "Blue");
        telemetry.update();

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
    }
}