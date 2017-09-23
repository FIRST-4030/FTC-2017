package org.firstinspires.ftc.teamcode;

/**
 * Created by robotics on 9/22/2017.
 *
 * by Bryan Cook
 *
 * Only used for the simpleMove(...) method in MechanumOpMode thus far...
 * or maybe not. I'm not the boss of you
 */

public enum Direction {

    FORWARD(Math.PI),
    BACKWARD(0.0),
    RIGHT(Math.PI / 2),
    LEFT(3 * Math.PI / 2);

    public double moveDirection;


    Direction(double moveDirection) {
    }



}
