package org.firstinspires.ftc.teamcode;
import java.lang.*;
import java.util.NoSuchElementException;

/**
 * Created by robotics on 10/14/2017.
 */

public enum AutoState {

    INIT,
    DRIVE_TO_JEWLS,
    PARSE_JEWELS,
    HIT_JEWEL,
    PARSE_PICTOGRAM,
    DRIVE_TO_TOWER,
    PLACE_BLOCK11,
    PLACE_BLOCK2,
    //DRIVE_TO_PILE,
    //COLLECT_BLOCKS,
    //DRIVE_TO_TOWER_FROM_PILE,
    DONE;

    private static final AutoState[] values = values();

    public AutoState prev()
    {
        int i = this.ordinal()-1;
        if(i < 0)
        {
            throw new NoSuchElementException();
        }
        return values[i];
    }

    public AutoState next()
    {
        int i = this.ordinal()+1;
        if(i >= values.length)
        {
            throw new NoSuchElementException();
        }
        return values[i];
    }

    public static final AutoState first = INIT;
    public static final AutoState last = DONE;
}
