package org.firstinspires.ftc.teamcode.vuforia;

import android.graphics.Bitmap;

import com.vuforia.Image;
import com.vuforia.PIXEL_FORMAT;

public class ImageFTC {
    public static final int FORMAT_DEFAULT_VUFORIA = PIXEL_FORMAT.RGBA8888;

    private Bitmap bitmap;
    private long timestamp = 0;

    public ImageFTC(Image img, int vuforiaFormat) {
        Bitmap.Config format;
        switch (vuforiaFormat) {
            case PIXEL_FORMAT.RGBA8888:
                format = Bitmap.Config.ARGB_8888;
                break;
            case PIXEL_FORMAT.RGB565:
                format = Bitmap.Config.RGB_565;
                break;
            default:
                throw new IllegalArgumentException("Unable to process Vuforia format: " + vuforiaFormat);
        }
        this.bitmap = Bitmap.createBitmap(img.getWidth(), img.getHeight(), format);
        this.bitmap.copyPixelsFromBuffer(img.getPixels());
        this.timestamp = System.currentTimeMillis();
    }

    public int getHeight() {
        return bitmap.getHeight();
    }

    public int getWidth() {
        return bitmap.getWidth();
    }

    public int getPixel(int x, int y) {
        return bitmap.getPixel(x, y);
    }

    @SuppressWarnings("unused")
    public Bitmap getBitmap() {
        return bitmap;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
