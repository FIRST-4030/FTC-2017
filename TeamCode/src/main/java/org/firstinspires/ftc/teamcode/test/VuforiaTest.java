package org.firstinspires.ftc.teamcode.test;

import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.teamcode.R;
import org.firstinspires.ftc.teamcode.config.VuforiaConfigs;
import org.firstinspires.ftc.teamcode.vuforia.ImageFTC;
import org.firstinspires.ftc.teamcode.vuforia.VuforiaFTC;

@Disabled
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
    private String lastMark = "<None>";
    private String lastImage = "<None>";
    private String lastRGB = "<None>";
    private boolean added = false;

    // Sensor reference types for our DriveTo callbacks
    enum SENSOR_TYPE {
        GYRO, ENCODER
    }

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
        boolean valid = false;
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
                    valid = true;
                    break;
                }
            }
            lastTarget = target;
            lastBearing = bearing;
            lastDistance = distance;
        }

        // Read the VuMark
        RelicRecoveryVuMark vuMark = RelicRecoveryVuMark.from(mark);
        switch (vuMark) {
            case UNKNOWN:
                break;
            case LEFT:
                lastMark = "Left";
                break;
            case CENTER:
                lastMark = "Center";
                break;
            case RIGHT:
                lastMark = "Right";
                break;
        }

        // Grab and optionally save an image
        vuforia.capture();
        ImageFTC image = vuforia.getImage();
        if (image != null && gamepad1.a) {
            image.savePNG("capture.png");
        }

        // RGB analysis of the upper-left-most pixel
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

        // Add UI elements to the video view
        if (!added) {
            added = true;
            Handler ui = new Handler(hardwareMap.appContext.getMainLooper());
            ui.post(new Runnable() {
                @Override
                public void run() {
                    View layout = View.inflate(hardwareMap.appContext, R.layout.activity_ftc_controller, null);
                    View view = layout.findViewById(R.id.cameraMonitorViewId);
                    TextView text = new TextView(hardwareMap.appContext);
                    text.setId(View.generateViewId());
                    text.setText("ZValue");
                    text.setTextColor(0);
                    text.setTextSize(36);
                    text.setBackgroundColor(128);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER);
                    ((FrameLayout) view.getParent()).addView(text, params);
                    System.err.println(text.getText());
                }
            });
        }

        /*
         * It's always safe to return after this; it should be nothing but auto-drive modes
         *
         * Typically you only want to trigger a single auto-drive mode at any given time so
         * be sure to return after selecting one
         */

        /*
         * Cut the loop short when we don't have a vision fix
         */
        if (!valid) {
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }
}