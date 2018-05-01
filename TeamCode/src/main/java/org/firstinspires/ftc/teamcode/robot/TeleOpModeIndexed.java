package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.buttons.BUTTON_TYPE;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.buttons.PAD_BUTTON;
import org.firstinspires.ftc.teamcode.robot.common.Common;
import org.firstinspires.ftc.teamcode.robot.common.Lights;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "TeleOp Indexed Lift")
public class TeleOpModeIndexed extends OpMode {

    // Drive speeds
    private final static float SCALE_FULL = 1.0f;
    private final static float SCALE_SLOW = SCALE_FULL * 0.5f;

    // Devices and subsystems
    private Robot robot = null;
    private ButtonHandler buttons;

    private org.firstinspires.ftc.teamcode.robot.lift.Lift lift;

    public enum LiftState implements OrderedEnum {
        LOW(org.firstinspires.ftc.teamcode.robot.lift.Lift.LOW),
        MID(org.firstinspires.ftc.teamcode.robot.lift.Lift.MIDDLE),
        HIGH(org.firstinspires.ftc.teamcode.robot.lift.Lift.HIGH);

        private LiftState(int height) {
            this.height = height;
        }

        public int height;

        public LiftState prev() {
            return OrderedEnumHelper.prev(this);
        }

        public LiftState next() {
            return OrderedEnumHelper.next(this);
        }

    }

    private LiftState liftIndex = LiftState.LOW;

    @Override
    public void init() {

        // Placate drivers
        telemetry.addData(">", "NOT READY");
        telemetry.update();

        // Init the common tasks elements
        robot = new Robot(hardwareMap, telemetry);

        lift = new org.firstinspires.ftc.teamcode.robot.lift.Lift();
        lift.init(robot.lift, robot.liftSwitch);

        // Register buttons
        buttons = new ButtonHandler(robot);
        buttons.register("SLOW-MODE", gamepad2, PAD_BUTTON.a, BUTTON_TYPE.TOGGLE);
        buttons.register("CLAW-" + CLAWS.TOP, gamepad1, PAD_BUTTON.right_trigger);
        buttons.register("CLAW-" + CLAWS.BOTTOM, gamepad1, PAD_BUTTON.left_trigger);
        buttons.register("ARM", gamepad1, PAD_BUTTON.a, BUTTON_TYPE.TOGGLE);
        buttons.register("LIGHTS", gamepad1, PAD_BUTTON.b, BUTTON_TYPE.TOGGLE);

        buttons.register("LIFT-UP", gamepad1, PAD_BUTTON.right_bumper);
        buttons.register("LIFT-DOWN", gamepad1, PAD_BUTTON.left_bumper);

        // Wait for the game to begin
        telemetry.addData(">", "Ready for game start");
        telemetry.update();

    }

    @Override
    public void start() {
        lift.start();
        robot.wheels.setTeleop(true);
        robot.jewelArm.setPosition(Common.JEWEL_ARM_RETRACT);
    }

    @Override
    public void loop() {

        // Update buttons
        buttons.update();

        // Move the robot
        driveBase();
        liftSystem();

        if (buttons.get("ARM")) robot.jewelArm.max();
        else robot.jewelArm.setPosition(Common.JEWEL_ARM_RETRACT);

        // Driver Feedback
        telemetry.addData("Wheels", robot.wheels.isAvailable());
        telemetry.addData("Teleop", robot.wheels.isTeleop());
        telemetry.addData("Slow Mode", buttons.get("SLOW-MODE"));
        telemetry.addData("Lift Height", robot.lift.getEncoder());
        telemetry.addData("Lift Index", liftIndex);
        telemetry.addData("Switch", robot.liftSwitch.get());
        telemetry.update();
    }

    public void driveBase() {
        if (buttons.get("SLOW-MODE")) {
            robot.wheels.setSpeedScale(SCALE_SLOW);
        } else {
            robot.wheels.setSpeedScale(SCALE_FULL);
        }
        robot.wheels.loop(gamepad1);
    }

    public void liftSystem() {

        // Lift
        if (buttons.get("LIFT-UP")) liftIndex = liftIndex.next();
        if (buttons.get("LIFT-DOWN")) liftIndex = liftIndex.prev();

        lift.set(liftIndex.height);

        // Lights
        if (buttons.get("LIGHTS")) {
            robot.lights.setPower(Lights.BRIGHTNESS_FULL);
        } else {
            robot.lights.setPower(Lights.BRIGHTNESS_OFF);
        }

        // Chassis-specific functions (claws and intakes)
        switch (robot.bot) {
            case WestCoast:
            case Mecanum: // doesn't exist
                // Intake motors
                float power = (gamepad1.left_trigger > gamepad1.right_trigger) ? (gamepad1.left_trigger) : (-gamepad1.right_trigger);
                for (Motor m : robot.intakes) {
                    m.setPower(power);
                }
                break;
            case WestCoastClaw:
                // Claws
                for (CLAWS claw : CLAWS.values()) {
                    if (buttons.get("CLAW-" + claw)) {
                        robot.claws[claw.ordinal()].toggle();
                        telemetry.addData("CLAW-" + claw, robot.claws[claw.ordinal()].getPostion());
                    }
                }
                break;

        }
    }

    public void stop() {
        lift.stop();
    }

}
