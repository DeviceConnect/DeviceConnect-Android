package org.deviceconnect.android.libmedia.streaming.video;

import android.hardware.camera2.CameraCharacteristics;

public class CameraVideoQuality extends VideoQuality {
    /**
     * カメラのタイプ.
     */
    private int mFacing = CameraCharacteristics.LENS_FACING_BACK;

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
}
