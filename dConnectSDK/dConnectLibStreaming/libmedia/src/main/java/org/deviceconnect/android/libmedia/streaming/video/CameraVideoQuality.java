package org.deviceconnect.android.libmedia.streaming.video;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;

import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperManager;

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
     * カメラの向きとディスプレイの向きでプレビューの横幅・縦幅を交換する必要があるか確認します.
     *
     * @param context コンテキスト
     * @return 交換が必要な場合は true、それ以外は false
     */
    public boolean isSwappedDimensions(Context context) {
        return Camera2WrapperManager.isSwappedDimensions(context, getFacing());
    }
}
