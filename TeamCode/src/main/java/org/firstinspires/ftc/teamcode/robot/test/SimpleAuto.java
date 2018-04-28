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
    private AutoDriver driver = new AutoDriver();
    private final PID pid = new PID();
    private org.firstinspires.ftc.teamcode.robot.lift.Lift lift;

    // Lift zero testing
    enum LIFT_STATE implements OrderedEnum {
        TIMEOUT,
        INIT,
        RETRACT,
        READY,
        DONE;

        public LIFT_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public LIFT_STATE next() {
            return OrderedEnumHelper.next(this);
        }
    }

    private LIFT_STATE liftState = LIFT_STATE.INIT;
    private static final int LIFT_TIMEOUT = 1500;
    // In general you should init false, but for testing start with nothing
    private boolean liftReady = true;
    private float liftTimeout = 0.0f;

    @Override
    public void init() {
        telemetry.addData(">", "Init…");
        telemetry.update();

        // Common init
        robot = new Robot(hardwareMap, telemetry);
        common = robot.common;

        lift = new org.firstinspires.ftc.teamcode.robot.lift.Lift();
        lift.init(robot.lift, robot.liftSwitch);

        // Buttons
        buttons = new ButtonHandler(robot);
        buttons.register("LOW", gamepad1, PAD_BUTTON.a);
        buttons.register("MIDDLE", gamepad1, PAD_BUTTON.b);
        buttons.register("HIGH", gamepad1, PAD_BUTTON.y);
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
        lift.start();
        robot.jewelArm.setPosition(Common.JEWEL_ARM_RETRACT);
        telemetry.clearAll();
    }

    @Override
    public void loop() {
        buttons.update();

        if (buttons.get("LOW")) lift.set(org.firstinspires.ftc.teamcode.robot.lift.Lift.LOW);
        if (buttons.get("MIDDLE")) lift.set(org.firstinspires.ftc.teamcode.robot.lift.Lift.MIDDLE);
        if (buttons.get("HIGH")) lift.set(org.firstinspires.ftc.teamcode.robot.lift.Lift.HIGH);

        telemetry.addData("Lift Height", robot.lift.getEncoder());
        telemetry.addData("Lift Switch", robot.liftSwitch.get());
        telemetry.update();

    }

    @Override
    public void stop() {
        lift.stop();
    }

}