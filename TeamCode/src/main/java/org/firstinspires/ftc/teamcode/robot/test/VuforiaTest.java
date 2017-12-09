package org.firstinspires.ftc.teamcode.robot.test;

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
    private int cornerInterval = 10;

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

        // Grab and optionally save an image
        robot.vuforia.capture();
        ImageFTC image = robot.vuforia.getImage();
        if (image != null) {
            lastImage = "(" + image.getWidth() + "," + image.getHeight() + ") " + image.getTimestamp();
            if (gamepad1.right_bumper) {
                common.drawJewelOutline(image);
                image.savePNG("vuforia-" + image.getTimestamp() + ".png");
            }
        }

        handleInput();

        // store the red values for later use in telemetry
        int[] sideReds = image != null ? common.getJewelReds(image) : new int[]{0, 0};

        // Driver feedback
        robot.vuforia.display(telemetry);
        telemetry.addData("Mark", lastMark);
        telemetry.addData("Target (" + lastTarget + ")", lastDistance + "mm @ " + lastBearing + "°");
        telemetry.addData("Image", lastImage);
        telemetry.addData("UL/LR Interval", cornerInterval);
        telemetry.addData("UL X,Y", CommonTasks.jewelUL[0] + ", " + CommonTasks.jewelUL[1]);
        telemetry.addData("LR X,Y", CommonTasks.jewelLR[0] + ", " + CommonTasks.jewelLR[1]);
        telemetry.addData("Left", sideReds[0]);
        telemetry.addData("Right", sideReds[1]);
        if (image != null)
            telemetry.addData("Reddest Side", common.leftJewelRed(image) ? "Left" : "Right");
        telemetry.addData("", "");
        telemetry.update();

    }

    // Once we get this working we should add all the "select a rectangle" stuff to the calibration mode
    // It will give gamepad2 something to do and will avoid the unncessary dynamic code in CommonTasks
    private void handleInput() {

        if (buttons.get("UL-INCREASE-X"))
            common.setJewelUL(new int[]{CommonTasks.jewelUL[0] + cornerInterval, CommonTasks.jewelUL[0]});
        else if (buttons.get("UL-DECREASE-X"))
            common.setJewelUL(new int[]{CommonTasks.jewelUL[0] - cornerInterval, CommonTasks.jewelUL[0]});
        if (buttons.get("UL-INCREASE-Y"))
            common.setJewelUL(new int[]{CommonTasks.jewelUL[1], CommonTasks.jewelUL[1] + cornerInterval});
        else if (buttons.get("UL-DECREASE-Y"))
            common.setJewelUL(new int[]{CommonTasks.jewelUL[1], CommonTasks.jewelUL[1] - cornerInterval});

        if (buttons.get("LR-INCREASE-X"))
            common.setJewelLR(new int[]{CommonTasks.jewelLR[0] + cornerInterval, CommonTasks.jewelLR[0]});
        else if (buttons.get("LR-DECREASE-X"))
            common.setJewelLR(new int[]{CommonTasks.jewelLR[0] - cornerInterval, CommonTasks.jewelLR[0]});
        if (buttons.get("LR-INCREASE-Y"))
            common.setJewelLR(new int[]{CommonTasks.jewelLR[1], CommonTasks.jewelLR[1] + cornerInterval});
        else if (buttons.get("LR-DECREASE-Y"))
            common.setJewelLR(new int[]{CommonTasks.jewelLR[1], CommonTasks.jewelLR[1] - cornerInterval});

        if (buttons.get("INCREASE-CHANGE-RATE")) cornerInterval += 1;
        else if (buttons.get("DECREASE-CHANGE-RATE"))
            cornerInterval = Math.max(1, cornerInterval - 1);

    }

}