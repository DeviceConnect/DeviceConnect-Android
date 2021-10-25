package org.deviceconnect.android.libmedia.streaming.video;

import android.hardware.camera2.CameraCharacteristics;
import android.util.Range;

public class CameraVideoQuality extends VideoQuality {
    /**
     * カメラのタイプ.
     */
    private int mFacing = CameraCharacteristics.LENS_FACING_BACK;

    /**
     * カメラのFPS.
     */
    private Range<Integer> mFps;

    /**
     * コンストラクタ.
     * @param mimeType マイムタイプ
     */
    public CameraVideoQuality(String mimeType) {
        super(mimeType);
    }

    /**
     * VideoQuality をコピーします.
     *
     * @param quality コピー元の VideoQuality
     */
    public void set(CameraVideoQuality quality) {
        super.set(quality);
        mFacing = quality.getFacing();
        if (quality.mFps != null) {
            mFps = new Range<>(quality.mFps.getLower(), quality.mFps.getUpper());
        }
    }

    /**
     * カメラのタイプを取得します.
     *
     * @return カメラのタイプ
     */
    public int getFacing() {
        return mFacing;
    }

    /**
     * カメラのタイプを設定します.
     *
     * 以下のタイプを設定することができます。
     * <p>
     * <ul>
     *     <li>{@link CameraCharacteristics#LENS_FACING_BACK}</li>
     *     <li>{@link CameraCharacteristics#LENS_FACING_FRONT}</li>
     *     <li>{@link CameraCharacteristics#LENS_FACING_EXTERNAL}</li>
     * </ul>
     * </p>
     *
     * @param facing カメラのタイプ
     */
    public void setFacing(int facing) {
        mFacing = facing;
    }

    /**
     * カメラの fps を取得します.
     *
     * 設定されていない場合は null を返却します.
     *
     * @return fps
     */
    public Range<Integer> getFps() {
        return mFps;
    }

    /**
     * カメラの fps を設定します.
     *
     * @param fps カメラの FPS
     */
    public void setFps(Range<Integer> fps) {
        mFps = fps;
    }
}
