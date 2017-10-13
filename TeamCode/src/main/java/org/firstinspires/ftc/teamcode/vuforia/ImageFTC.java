package org.firstinspires.ftc.teamcode.vuforia;

import android.graphics.Bitmap;
import android.os.Environment;

import com.vuforia.Image;
import com.vuforia.PIXEL_FORMAT;

import java.io.File;
import java.io.FileOutputStream;

public class ImageFTC {
    public static final int FORMAT_VUFORIA_DEFAULT = PIXEL_FORMAT.RGBA8888;
    public static final String SAVE_DIR_DEFAULT = Environment.DIRECTORY_PICTURES;
    public static final int SAVE_QUALITY_DEFAULT = 100;

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

    public Bitmap getBitmap() {
        return bitmap;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean save(File file, Bitmap.CompressFormat format, int quality) {
        boolean success;
        try {
            FileOutputStream out = new FileOutputStream(file);
            success = bitmap.compress(format, quality, out);
            out.close();
        } catch (Exception e) {
            return false;
        }
        return success;
    }

    public boolean savePNG(String name) {
        File file = new File(Environment.getExternalStoragePublicDirectory(SAVE_DIR_DEFAULT), name);
        return save(file, Bitmap.CompressFormat.PNG, SAVE_QUALITY_DEFAULT);
    }
}
