package org.firstinspires.ftc.teamcode.robot.common;

import android.graphics.Color;

import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.field.Field;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;
import org.firstinspires.ftc.teamcode.vuforia.ImageFTC;

public class Jewel implements CommonTask {
    private static final boolean DEBUG = true;

    // Drive constants
    public static final double ARM_DELAY = 0.5d;
    public static final int PIVOT_DEGREES = 10;

    // Image constants
    private int[] IMAGE_MAX = new int[]{1279, 719};
    public static final Field.AllianceColor PIVOT_CCW_COLOR = Field.AllianceColor.BLUE;

    // Jewel parse default values
    public int[] UL = new int[]{0, 0};
    public int[] LR = new int[]{800, 300};

    // TODO: Save/load to disk, so changes are persistent without code updates
    // TODO: Alert if the default values differ from the saved values

    // Runtime
    private final Robot robot;
    private final Common common;
    private ImageFTC image;
    private Boolean isLeftRed;
    private PARSE_STATE parseState;
    private HIT_STATE hitState;

    public Jewel(Robot robot, Common common) {
        this.robot = robot;
        this.common = common;
        parseState = PARSE_STATE.values()[0];
        hitState = HIT_STATE.values()[0];
        isLeftRed = null;
    }

    public void setImage(ImageFTC image) {
        this.image = image;
        if (image != null) {
            int[] max = new int[2];
            max[0] = image.getWidth();
            max[1] = image.getHeight();
            makeAreaSafe(max, true);
            makeAreaSafe(max, false);
            IMAGE_MAX = max;
            isLeftRed = null;
        }
    }

    public ImageFTC getImage() {
        return image;
    }

    private void drawOutline() {

        // Vertical lines at UL[x] and LR[x]
        for (int i = UL[1]; i <= LR[1]; i++) {
            image.getBitmap().setPixel(UL[0], i, Color.GREEN);
            image.getBitmap().setPixel(UL[0] + 1, i, Color.GREEN);
            image.getBitmap().setPixel(LR[0], i, Color.GREEN);
            image.getBitmap().setPixel(LR[0] - 1, i, Color.GREEN);
        }

        // Horizontal lines at UL[y] and LR[y]
        for (int i = UL[0]; i <= LR[0]; i++) {
            image.getBitmap().setPixel(i, UL[1], Color.GREEN);
            image.getBitmap().setPixel(i, UL[1] + 1, Color.GREEN);
            image.getBitmap().setPixel(i, LR[1], Color.GREEN);
            image.getBitmap().setPixel(i, LR[1] - 1, Color.GREEN);
        }

        // Vertical line at the center divsion
        int middleX = ((LR[0] - UL[0]) / 2) + UL[0];
        for (int i = UL[1]; i <= LR[1]; i++) {
            image.getBitmap().setPixel(middleX, i, Color.RED);
            image.getBitmap().setPixel(middleX + 1, i, Color.RED);
        }
    }

    public boolean isLeftRed() {
        if (isLeftRed != null) {
            return isLeftRed.booleanValue();
        }
        if (!isAvailable()) {
            return false;
        }

        // Average color of each half
        int middleX = (LR[0] + UL[0]) / 2;
        int left = Color.red(image.rgb(new int[]{middleX + 1, UL[1]}, LR));
        int right = Color.red(image.rgb(UL, new int[]{middleX, LR[1]}));
        if (DEBUG) {
            robot.telemetry.log().add("Jewel Reds: " + left + ", " + right);
        }

        // Outline for humans
        drawOutline();

        return (left > right);
    }

    public boolean pivotCCW(Field.AllianceColor alliance) {
        return (isLeftRed()) == (alliance == PIVOT_CCW_COLOR);
    }

    public boolean isAvailable() {
        if (image == null) {
            robot.telemetry.log().add(this.getClass().getName() + ": Falling back to default image");
            setImage(robot.vuforia.getImage());
        }
        if (image == null) {
            robot.telemetry.log().add(this.getClass().getName() + ": No image available");
        }
        return image != null;
    }

    public void changeArea(boolean isLR, boolean isX, int interval) {
        int index = isX ? 0 : 1;
        int[] src = isLR ? LR : UL;
        int[] dst = new int[2];

        System.arraycopy(dst, 0, src, 0, 2);
        dst[index] += interval;
        makeAreaSafe(dst, isLR);

        if (isLR) {
            this.LR = dst;
        } else {
            this.UL = dst;
        }
    }

    public void makeAreaSafe(int[] a, boolean isLR) {
        if (isLR) {
            a[0] = Math.max(UL[0] + 1, Math.min(IMAGE_MAX[0], a[0]));
            a[1] = Math.max(UL[1] + 1, Math.min(IMAGE_MAX[1], a[1]));
        } else {
            a[0] = Math.max(0, Math.min(LR[0] - 1, a[0]));
            a[1] = Math.max(0, Math.min(LR[1] - 1, a[1]));
        }
    }

    public AutoDriver hit(Field.AllianceColor alliance) {
        AutoDriver driver = new AutoDriver();
        if (DEBUG) {
            robot.telemetry.log().add("hitState: " + hitState);
        }

        switch (hitState) {
            case INIT:
                hitState = hitState.next();
                break;
            case DEPLOY_ARM:
                robot.jewelArm.max();
                driver.interval = ARM_DELAY;
                hitState = hitState.next();
                break;
            case HIT_JEWEL:
                //turns -90 if we're hitting the left jewel, 90 if we're hitting the right.
                driver.drive = common.drive.degrees((pivotCCW(alliance) ? -1 : 1) * PIVOT_DEGREES);
                hitState = hitState.next();
                break;
            case RETRACT_ARM:
                robot.jewelArm.setPosition(Common.JEWEL_ARM_RETRACT);
                driver.interval = ARM_DELAY;
                hitState = hitState.next();
                break;
            case DONE:
                driver.done = true;
                break;
        }

        return driver;
    }

    public AutoDriver parse() {
        AutoDriver driver = new AutoDriver();
        if (DEBUG) {
            robot.telemetry.log().add("parseState: " + parseState);
        }

        switch (parseState) {
            case INIT:
                parseState = parseState.next();
                break;
            case ENABLE_CAPTURE:
                robot.vuforia.enableCapture(true);
                parseState = parseState.next();
                break;
            case WAIT_FOR_IMAGE:
                if (image == null) {
                    robot.vuforia.capture();
                    image = robot.vuforia.getImage();
                } else {
                    setImage(image);
                    parseState = parseState.next();
                }
                break;
            case DISABLE_CAPTURE:
                robot.vuforia.enableCapture(false);
                parseState = parseState.next();
                break;
            case PARSE_JEWEL:
                isLeftRed();
                // Save the parsed image for future analysis
                image.savePNG("auto-" + System.currentTimeMillis() + ".png");
                parseState = parseState.next();
                break;
            case DONE:
                driver.done = true;
                break;
        }

        return driver;
    }

    enum HIT_STATE implements OrderedEnum {
        INIT,
        DEPLOY_ARM,         // Move the arm down so we can hit the jewel
        HIT_JEWEL,          // Pivot to hit the correct jewel
        RETRACT_ARM,        // Retract the arm so we don't accidentally hit the jewels again
        DONE;

        public HIT_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public HIT_STATE next() {
            return OrderedEnumHelper.next(this);
        }
    }

    enum PARSE_STATE implements OrderedEnum {
        INIT,
        ENABLE_CAPTURE,     // Enable vuforia image capture
        WAIT_FOR_IMAGE,     // Make sure we don't try to do anything before Vuforia returns an image to analyze.
        DISABLE_CAPTURE,    // Disable vuforia capture so we run faster (?)
        PARSE_JEWEL,        // Parse which jewel is on which side
        DONE;

        public PARSE_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public PARSE_STATE next() {
            return OrderedEnumHelper.next(this);
        }
    }
}
