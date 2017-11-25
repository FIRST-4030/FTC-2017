package org.firstinspires.ftc.teamcode.wheels;

import java.util.ArrayList;

/**
 * Created by robotics on 11/24/2017.
 */

public enum WHEEL_DIAGONAL{

    FRONT_LEFT,
    FRONT_RIGHT;

    public WheelMotor[] getWheels(WheelsConfig config){

        ArrayList<WheelMotor> wheels = new ArrayList<WheelMotor>();

        for(WheelMotor motor : config.motors){

            if(this == FRONT_LEFT && (
                    (motor.side == MOTOR_SIDE.LEFT && motor.end == MOTOR_END.FRONT)
                            || (motor.side == MOTOR_SIDE.RIGHT && motor.end == MOTOR_END.BACK)
            )){

                wheels.add(motor);

            }



            if(this == FRONT_LEFT && (
                    (motor.side == MOTOR_SIDE.RIGHT && motor.end == MOTOR_END.FRONT)
                            || (motor.side == MOTOR_SIDE.LEFT && motor.end == MOTOR_END.BACK)
            )) {

                wheels.add(motor);

            }
        }

        return (WheelMotor[]) wheels.toArray();

    }

}
