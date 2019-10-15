package org.deviceconnect.android.deviceplugin.theta.core;

public class SphericalViewParam {

    private double mCameraX;

    private double mCameraY;

    private double mCameraZ;

    private double mCameraRoll;

    private double mCameraPitch;

    private double mCameraYaw;

    private double mSphereSize = 1.0;

    private int mWidth = 600;

    private int mHeight = 400;

    private double mFOV = 90.0;

    private boolean mIsVRMode;

    private boolean mIsStereo;

    private double mFrameRate;

    public double getCameraX() {
        return mCameraX;
    }

    public void setCameraX(double x) {
        mCameraX = x;
    }

    public double getCameraY() {
        return mCameraY;
    }

    public void setCameraY(double y) {
        mCameraY = y;
    }

    public double getCameraZ() {
        return mCameraZ;
    }

    public void setCameraZ(double z) {
        mCameraZ = z;
    }

    public double getCameraRoll() {
        return mCameraRoll;
    }

    public void setCameraRoll(double roll) {
        mCameraRoll = roll;
    }

    public double getCameraPitch() {
        return mCameraPitch;
    }

    public void setCameraPitch(double pitch) {
        mCameraPitch = pitch;
    }

    public double getCameraYaw() {
        return mCameraYaw;
    }

    public void setCameraYaw(double yaw) {
        mCameraYaw = yaw;
    }

    public double getSphereSize() {
        return mSphereSize;
    }

    public void setSphereSize(double sphereSize) {
        mSphereSize = sphereSize;
    }

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
