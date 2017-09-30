package org.firstinspires.ftc.teamcode;

/**
 * Created by robotics on 9/22/2017.
 */

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="MechanumTeleOpMode1", group="MechanumOpMode")
public class MechanumTeleOpMode1 extends MechanumOpMode{

    @Override
    public void loop() {

        double lStickX = gamepad1.left_stick_x;
        double lStickY = gamepad1.left_stick_y;

        double speed = Math.sqrt(Math.pow(lStickX, 2) + Math.pow(lStickY, 2));
        double moveAngle = Math.atan2(lStickY, lStickX);

        double rotationSpeed = 0;

        if(gamepad1.left_trigger > 0) rotationSpeed = gamepad1.left_trigger;
        else if(gamepad1.right_trigger > 0) rotationSpeed = gamepad1.right_trigger;

        move(speed, moveAngle, rotationSpeed);

        Direction moveDirection = null;

        if(gamepad1.dpad_down) moveDirection = Direction.BACKWARD;
        if(gamepad1.dpad_left) moveDirection = Direction.LEFT;
        if(gamepad1.dpad_right) moveDirection = Direction.RIGHT;
        if(gamepad1.dpad_up) moveDirection = Direction.FORWARD;

        if(moveDirection != null){
            simpleMove(moveDirection, 1);
        } else {
            stop();
        }

    }

}
