package org.deviceconnect.android.deviceplugin.theta.core;

public class SphericalViewParam {

    private int mWidth;

    private int mHeight;

    private double mFOV = 90.0;

    private boolean mIsVRMode;

    private boolean mIsStereo;

    private double mFrameRate;

    // TODO Add other parameters.

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        this.mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        this.mHeight = height;
    }

    public double getFOV() {
        return mFOV;
    }

    public void setFOV(double fov) {
        this.mFOV = fov;
    }

    public boolean isVRMode() {
        return mIsVRMode;
    }

    public void setVRMode(boolean isVRMode) {
        this.mIsVRMode = isVRMode;
    }

    public boolean isStereo() {
        return mIsStereo;
    }

    public void setStereo(boolean isStereo) {
        this.mIsStereo = isStereo;
    }

    public double getFrameRate() {
        return mFrameRate;
    }

    public void setFrameRate(double frameRate) {
        this.mFrameRate = frameRate;
    }
}
