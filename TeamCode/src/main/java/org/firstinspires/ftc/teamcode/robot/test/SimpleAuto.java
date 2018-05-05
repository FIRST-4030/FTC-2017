package org.firstinspires.ftc.teamcode.robot.test;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.buttons.BUTTON_TYPE;
import org.firstinspires.ftc.teamcode.buttons.PAD_BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.driveto.PID;
import org.firstinspires.ftc.teamcode.field.Field;
import org.firstinspires.ftc.teamcode.field.VuforiaConfigs;
import org.firstinspires.ftc.teamcode.robot.auto.Jewel;
import org.firstinspires.ftc.teamcode.robot.common.Common;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.robot.common.Drive;
import org.firstinspires.ftc.teamcode.robot.common.Lift;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;
import org.firstinspires.ftc.teamcode.utils.Round;


@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Simple Auto", group = "Test")
public class SimpleAuto extends OpMode {

    // Devices and subsystems
    private Robot robot = null;
    private Common common = null;
    private ButtonHandler buttons = null;

    public Sound lift = new Sound();

    @Override
    public void init() {
        telemetry.addData(">", "Init…");
        telemetry.update();

        // Common init
        robot = new Robot(hardwareMap, telemetry);
        common = robot.common;



        // Buttons
        buttons = new ButtonHandler(robot);
        buttons.register("PAUSE", gamepad1, PAD_BUTTON.a);
        buttons.register("NEXT", gamepad1, PAD_BUTTON.dpad_right);
        buttons.register("PREV", gamepad1, PAD_BUTTON.dpad_left);

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
        robot.jewelArm.setPosition(Common.JEWEL_ARM_RETRACT);
        telemetry.clearAll();
        lift.init();
    }

    @Override
    public void loop() {
        buttons.update();

        if (buttons.get("PAUSE")) {
            if (lift.lift.isPlaying()) lift.lift.pause();
            else lift.lift.start();
        }

        if (buttons.get("NEXT")) lift.next();
        if (buttons.get("PREV")) lift.prev();

        telemetry.addData("CurrentSong", lift.currentSound);
        telemetry.update();

    }

    @Override
    public void stop() {
        lift.stop();
    }

}