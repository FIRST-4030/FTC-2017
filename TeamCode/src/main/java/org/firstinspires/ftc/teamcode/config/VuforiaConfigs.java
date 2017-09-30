package org.firstinspires.ftc.teamcode.config;

import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.teamcode.field.Field;
import org.firstinspires.ftc.teamcode.vuforia.VuforiaTarget;

public class VuforiaConfigs {
    public static final String AssetName = "RelicVuMark";
    public static final int TargetCount = 1;

    static public VuforiaTarget Bot() {
        // TODO: This location and rotation is imaginary, but should at least be close.
        return new VuforiaTarget(
                "Phone", null,
                new float[]{(18 * Field.MM_PER_INCH) / 2, 0, 0},
                new float[]{-90, 0, 0},
                AxesOrder.YZY
        );
    }

    static public VuforiaTarget[] Field() {
        // TODO: These targets, locations and rotations are imaginary.
        float[] ROTATION_BLUE = {90, 270, 0};
        float[] ADJUST_BLUE = {-300, 0, 0};

        int X_BLUE = Field.FIELD_WIDTH / 2;
        int OFFSET_NEAR = (int) (12 * Field.MM_PER_INCH);

        return new VuforiaTarget[]{new VuforiaTarget(
                "VuMark", Field.AllianceColor.BLUE,
                new float[]{X_BLUE, -OFFSET_NEAR, 0},
                ADJUST_BLUE, ROTATION_BLUE
        )};
    }
}
