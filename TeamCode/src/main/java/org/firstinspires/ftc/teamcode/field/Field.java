package org.firstinspires.ftc.teamcode.field;

public class Field {
    public static final float MM_PER_INCH = 25.4f;
    public static final int FIELD_WIDTH = (int) ((12 * 12 - 2) * MM_PER_INCH);

    public enum AllianceColor {
        RED, BLUE;

        public static AllianceColor opposite(AllianceColor color) {
            switch (color) {
                case RED:
                    color = AllianceColor.BLUE;
                    break;
                case BLUE:
                    color = AllianceColor.RED;
                    break;
            }
            return color;
        }
    }
}
