package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.teamcode.vuforia.VuforiaFTC;
import org.firstinspires.ftc.teamcode.config.VuforiaConfigs;

@SuppressWarnings("unused")
@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "Vuforia Test", group = "Test")
public class VuforiaTest extends OpMode {

    // Numeric constants
    private final static int FULL_CIRCLE = 360;

    // Dynamic things we need to remember
    private VuforiaFTC vuforia;
    private int lastBearing = 0;
    private int lastDistance = 0;
    private String lastTarget = "<None>";
    private String lastMark = "<None>";
    private VuforiaTrackable mark;

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
        mark = vuforia.getTrackable(vuforia.getTargetIndex("VuMark"));

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
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    @Override
    public void loop() {

        // Driver feedback
        vuforia.display(telemetry);
        telemetry.addData("Target (" + lastTarget + ")", lastDistance + "mm @ " + lastBearing + "°");
        telemetry.addData("Mark", lastMark);
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
            return;
        }
    }
}