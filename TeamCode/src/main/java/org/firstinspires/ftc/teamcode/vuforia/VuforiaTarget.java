package org.firstinspires.ftc.teamcode.vuforia;

import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.teamcode.field.Field;

public class VuforiaTarget {
    private static final int NUM_DIMENSIONS = 3;

    public final String name;
    public final Field.AllianceColor color;
    public final float[] raw;
    public final float[] rotation;
    public final AxesOrder axesOrder;
    public final int[] adjusted;

    private VuforiaTarget(String name, Field.AllianceColor color, float[] location, float[] offset, float[] rotation, AxesOrder axesOrder) {
        this.name = name;
        this.color = color;
        this.raw = location;
        float[] offset1;
        if (offset != null) {
            offset1 = offset;
        } else {
            offset1 = new float[NUM_DIMENSIONS];
        }
        this.rotation = rotation;
        if (axesOrder != null) {
            this.axesOrder = axesOrder;
        } else {
            this.axesOrder = AxesOrder.XZX;
        }

        this.adjusted = new int[NUM_DIMENSIONS];
        for (int i = 0; i < NUM_DIMENSIONS; i++) {
            this.adjusted[i] = (int) (this.raw[i] + offset1[i]);
        }
    }

    public VuforiaTarget(String name, Field.AllianceColor color, float[] location, float[] offset, float[] rotation) {
        this(name, color, location, offset, rotation, null);
    }

    public VuforiaTarget(String name, Field.AllianceColor color, float[] location, float[] rotation, AxesOrder axesOrder) {
        this(name, color, location, null, rotation, axesOrder);
    }
}