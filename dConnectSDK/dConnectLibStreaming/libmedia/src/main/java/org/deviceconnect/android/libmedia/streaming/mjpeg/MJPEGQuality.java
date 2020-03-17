package org.deviceconnect.android.libmedia.streaming.mjpeg;

import android.hardware.camera2.CameraCharacteristics;

public class MJPEGQuality {
    private int mFacing = CameraCharacteristics.LENS_FACING_BACK;

    private int mWidth = 480;
    private int mHeight = 640;
    private int mQuality = 60;
    private int mFrameRate = 30;

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public int getQuality() {
        return mQuality;
    }

    public void setQuality(int quality) {
        mQuality = quality;
    }

    public int getFrameRate() {
        return mFrameRate;
    }

    public void setFrameRate(int frameRate) {
        mFrameRate = frameRate;
    }

    public int getFacing() {
        return mFacing;
    }

    public void setFacing(int facing) {
        mFacing = facing;
    }
}
