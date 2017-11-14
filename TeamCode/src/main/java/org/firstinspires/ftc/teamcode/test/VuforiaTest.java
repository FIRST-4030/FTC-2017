package org.firstinspires.ftc.teamcode.test;

import android.graphics.Color;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.teamcode.config.VuforiaConfigs;
import org.firstinspires.ftc.teamcode.vuforia.ImageFTC;
import org.firstinspires.ftc.teamcode.vuforia.VuforiaFTC;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "Vuforia Test", group = "Test")
public class VuforiaTest extends OpMode {
    private final static int COLOR_SLICES = 5;

    // Numeric constants
    private final static int FULL_CIRCLE = 360;

    // Dynamic things we need to remember
    private VuforiaFTC vuforia;
    private VuforiaTrackable mark;
    private int lastBearing = 0;
    private int lastDistance = 0;
    private String lastTarget = "<None>";
    private RelicRecoveryVuMark lastMark = RelicRecoveryVuMark.UNKNOWN;
    private String lastImage = "<None>";
    private String lastRGB = "<None>";

    @Override
    public void init() {

        // Placate drivers; sometimes VuforiaFTC is slow to init
        telemetry.addData(">", "Initializing...");
        telemetry.update();

        // Vuforia
        vuforia = new VuforiaFTC(VuforiaConfigs.AssetName, VuforiaConfigs.TargetCount,
                VuforiaConfigs.Field(), VuforiaConfigs.Bot());
        vuforia.init();
        mark = vuforia.getTrackable("VuMark");

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

        // Start Vuforia tracking
        vuforia.start();
        vuforia.enableCapture(true);
    }

    @Override
    public void loop() {

        // Driver feedback
        vuforia.display(telemetry);
        telemetry.addData("Mark", lastMark);
        telemetry.addData("RGB", lastRGB);
        telemetry.addData("Target (" + lastTarget + ")", lastDistance + "mm @ " + lastBearing + "°");
        telemetry.addData("Image", lastImage);
        telemetry.update();

        // Update our location and target info
        vuforia.track();

        // Collect data about the first visible target
        String target = null;
        int bearing = 0;
        int distance = 0;
        if (!vuforia.isStale()) {
            for (String t : vuforia.getVisible().keySet()) {
                if (vuforia.getVisible(t)) {
                    target = t;
                    int index = vuforia.getTargetIndex(t);
                    bearing = vuforia.bearing(index);
                    distance = vuforia.distance(index);
                    break;
                }
            }
            lastTarget = target;
            lastBearing = bearing;
            lastDistance = distance;
        }

        // Read the VuMark
        RelicRecoveryVuMark lastMark = RelicRecoveryVuMark.from(mark);

        // Grab and optionally save an image
        vuforia.capture();
        ImageFTC image = vuforia.getImage();
        if (image != null && gamepad1.a) {
            image.savePNG("vuforia-" + image.getTimestamp() + ".png");
        }

        // RGB analysis of each of COLOR_SLICES number of equal slices of the image
        // image.rgb() takes an upper-left and lower-right pixel location and returns the
        // average color of all pixels in that rectangle.
        if (image != null) {
            lastImage = "(" + image.getWidth() + "," + image.getHeight() + ") " + image.getTimestamp();
            int slice = image.getWidth() / COLOR_SLICES;
            lastRGB = "";
            for (int i = 0; i < COLOR_SLICES; i++) {
                int rgb = image.rgb(
                        new int[]{(i * slice), 0},
                        new int[]{((i + 1) * slice) - 1, image.getHeight() - 1});
                lastRGB += " (" + Color.red(rgb) + "," + Color.green(rgb) + "," + Color.blue(rgb) + ")";
            }
        }
    }
}