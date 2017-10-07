package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

public class MotorTestOpMode extends OpMode {

    public DcMotor motor;

    @Override
    public void init() {

        motor = hardwareMap.dcMotor.get("motor");

    }

    @Override
    public void loop() {

        double power;

        if(gamepad1.a) power = 2;
        else if(gamepad1.b) power = 1.5;
        else if(gamepad1.y) power = 1;
        else if(gamepad1.x) power = .5;
        else power = 0;

        motor.setPower(power);
        telemetry.addData("power", .5);
        telemetry.update();

    }
}
