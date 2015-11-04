package org.deviceconnect.android.deviceplugin.theta.core;

public class SphericalViewParam {

    private int mWidth;

    private int mHeight;

    private double mFOV;

    private boolean mIsVRMode;

    private double mFrameRate;

    // TODO Add other parameters.

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int mWidth) {
        this.mWidth = mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int mHeight) {
        this.mHeight = mHeight;
    }

    public double getFOV() {
        return mFOV;
    }

    public void setFOV(double mFOV) {
        this.mFOV = mFOV;
    }

    public boolean isVRMode() {
        return mIsVRMode;
    }

    public void setVRMode(boolean mIsVRMode) {
        this.mIsVRMode = mIsVRMode;
    }

    public double getFrameRate() {
        return mFrameRate;
    }

    public void setFrameRate(double mFrameRate) {
        this.mFrameRate = mFrameRate;
    }
}
