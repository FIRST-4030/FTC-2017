package org.firstinspires.ftc.teamcode;

    import com.qualcomm.robotcore.eventloop.opmode.OpMode;
    import com.qualcomm.robotcore.hardware.DcMotor;
/**
 * Created by Alex and Brian on 10/9/2017.
 *
 * abstract class to provide west coast movement methods.
 *
 */

public abstract class WestCoastOpMode extends OpMode{

    public DcMotor LWheel1;
    public DcMotor LWheel2;
    public DcMotor RWheel1;
    public DcMotor RWheel2;
    public DcMotor Lift;

    public void init() {
        LWheel1 = hardwareMap.dcMotor.get("ML1");
        LWheel2 = hardwareMap.dcMotor.get("ML2");
        RWheel1 = hardwareMap.dcMotor.get("MR1");
        RWheel2 = hardwareMap.dcMotor.get("MR2");
        Lift = hardwareMap.dcMotor.get("LM1");
    }

    /**
     * Turn a DC motor at a power range of -1 to 1
     *
     * Throws an IllegalArgumentException if the power parameter is not between -1 and 1 inclusive
     *
     * @param motor The motor to turn
     * @param power The power to turn the motor at. Between -1 and 1
     */

    public void turnMotor(DcMotor motor, double power) {
        if (Math.abs(power) > 1) {
            throw new IllegalArgumentException("turnmotor: the power value must be between -1 and 1 inclusive");
        }

        motor.setPower(power);
    }

    public void liftMotor(DcMotor liftMotor, double power){
        if (Math.abs(power) > 1) {
            throw new IllegalArgumentException("liftMotor: the power value must be between -1 and 1 inclusive");
        }
    }
}
