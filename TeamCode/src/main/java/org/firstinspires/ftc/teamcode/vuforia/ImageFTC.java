package org.firstinspires.ftc.teamcode.vuforia;

import java.nio.ByteBuffer;

public class ImageFTC {
    private static final int BPP_DEFAULT = 3;

    private ByteBuffer pixels = null;
    private int height = 0;
    private int width = 0;
    private int bpp = BPP_DEFAULT;

    public ImageFTC(ByteBuffer pixels, int height, int width, int bpp) {
        this.pixels = pixels.duplicate();
        this.pixels = pixels;
        this.height = height;
        this.width = width;
        this.bpp = bpp;
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

    public int getStride() {
        return bpp * width;
    }

    public ByteBuffer getPixels() {
        return pixels;
    }
}
