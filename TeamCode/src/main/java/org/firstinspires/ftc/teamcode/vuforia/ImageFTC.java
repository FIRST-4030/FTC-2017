package org.firstinspires.ftc.teamcode.vuforia;

import java.nio.ByteBuffer;

public class ImageFTC {
    private static final int BPP_DEFAULT = 3;

    private ByteBuffer pixels = null;
    private int height = 0;
    private int width = 0;
    private int bpp = BPP_DEFAULT;
    private long timestamp = 0;

    public ImageFTC(ByteBuffer pixels, int height, int width, int bpp) {
        if (pixels.isDirect()) {
            this.pixels = ByteBuffer.allocateDirect(pixels.remaining());
        } else {
            this.pixels = ByteBuffer.allocate(pixels.remaining());
        }
        this.pixels.put(pixels);
        this.pixels.order(pixels.order());

        this.height = height;
        this.width = width;
        this.bpp = bpp;
        this.timestamp = System.currentTimeMillis();
    }

    public ImageFTC(ByteBuffer bytes, int height, int width) {
        this(bytes, height, width, BPP_DEFAULT);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getBpp() {
        return bpp;
    }

    public int getStride() {
        return bpp * width;
    }

    public ByteBuffer getPixels() {
        return pixels;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
