package org.firstinspires.ftc.teamcode;

/**
 * Created by robotics on 10/14/2017.
 */

public enum AutoState {

    // Initialization
    INIT, // WestCoastAuto

    // Knocking the gem off the stand
    DETECT_GEM, // WestCoastAuto
    HIT_GEM, // Color specific

    // Cryptobox happy funtimes
    MOVE_TO_CRYPTOBOX, // Location Specific
    PLACE_BLOCK_1, // First block being held   Location Specific
    PLACE_BLOCK_2, // Second block being held   Location Specific
    CRYPTOBOX_RETURN, // Location Specific

    // Grab dat block
    MOVE_TO_BLOCKS, // Location Specific
    GRAB_1, // First block space   WestCoastAuto
    GRAB_2, // Second block space   WestCoastAuto

    // Move back to cryptobox
    // Place blocks

    // End
    FINISH // WestCoastAuto

}
