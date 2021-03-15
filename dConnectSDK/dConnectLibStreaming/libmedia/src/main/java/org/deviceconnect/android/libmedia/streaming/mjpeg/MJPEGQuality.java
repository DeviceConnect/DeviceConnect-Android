package org.deviceconnect.android.libmedia.streaming.mjpeg;

import android.hardware.camera2.CameraCharacteristics;

public class MJPEGQuality {
    private int mFacing = CameraCharacteristics.LENS_FACING_BACK;
    private int mWidth = 480;
    private int mHeight = 640;
    private int mQuality = 60;
    private int mFrameRate = 30;

    /**
     * Motion JPEG の横幅を取得します.
     *
     * @return Motion JPEG の横幅
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * Motion JPEG の横幅を設定します.
     *
     * @param width Motion JPEG の横幅
     */
    public void setWidth(int width) {
        mWidth = width;
    }

    /**
     * Motion JPEG の縦幅を取得します.
     *
     * @return Motion JPEG の縦幅
     */
    public int getHeight() {
        return mHeight;
    }

    /**
     * Motion JPEG の縦幅を設定します.
     *
     * @param height Motion JPEG の縦幅
     */
    public void setHeight(int height) {
        mHeight = height;
    }

    /**
     * Motion JPEG のクオリティを取得します.
     *
     * @return Motion JPEG のクオリティ
     */
    public int getQuality() {
        return mQuality;
    }

    /**
     * Motion JPEG のクオリティを設定します.
     *
     * 0 から 100 の範囲で指定することができます。
     *
     * @param quality Motion JPEG のクオリティ
     */
    public void setQuality(int quality) {
        if (quality < 0) {
            throw new IllegalArgumentException("quality cannot set a negative value.");
        }
        if (quality > 100) {
            throw new IllegalArgumentException("quality cannot set above 100.");
        }
        mQuality = quality;
    }

    /**
     * フレームレートを取得します.
     *
     * @return フレームレート
     */
    public int getFrameRate() {
        return mFrameRate;
    }

    /**
     * フレームレートを設定します.
     *
     * @param frameRate フレームレート
     */
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
