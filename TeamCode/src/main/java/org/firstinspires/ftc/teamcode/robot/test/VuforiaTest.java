package org.firstinspires.ftc.teamcode.robot.test;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.field.VuforiaConfigs;
import org.firstinspires.ftc.teamcode.robot.Robot;

@Disabled
@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "Vuforia Test", group = "Test")
public class VuforiaTest extends OpMode {

    // Devices and subsystems
    private Robot robot;
    private ButtonHandler buttons;

    // Dynamic things we need to remember
    private int lastBearing = 0;
    private int lastDistance = 0;
    private String lastTarget = "<None>";
    private RelicRecoveryVuMark lastMark = RelicRecoveryVuMark.UNKNOWN;

    @Override
    public void init() {

        // Init the robot
        robot = new Robot(hardwareMap, telemetry);
        buttons = new ButtonHandler(robot);

        // Wait for the game to begin
        telemetry.addData(">", "Ready for game start");
        telemetry.update();
    }

    @Override
    public void start() {
        telemetry.clearAll();

        // Start Vuforia tracking
        robot.vuforia.start();
    }

    @Override
    public void loop() {
        buttons.update();

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

        // Driver feedback
        robot.vuforia.display(telemetry);
        telemetry.addData("Mark", lastMark);
        telemetry.addData("Target (" + lastTarget + ")", lastDistance + "mm @ " + lastBearing + "°");
        telemetry.update();
    }
}