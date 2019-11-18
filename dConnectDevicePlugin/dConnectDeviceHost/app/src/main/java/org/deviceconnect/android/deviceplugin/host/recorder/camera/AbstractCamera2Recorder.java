/*
 AbstractCamera2Recorder.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

import androidx.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.camera.Camera2Helper;
import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;

/**
 * Camera2 API で実装されるレコーダーの基底クラス.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class AbstractCamera2Recorder extends AbstractPreviewServerProvider {

    /**
     * ログ出力用タグ.
     */
    private static final String TAG = "host.dplugin";

    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * NotificationID のベースを定義.
     */
    private static final int NOTIFICATION_ID_BASE = 1010;

    /**
     * {@link CameraManager} のインスタンス.
     */
    protected final CameraManager mCameraManager;

    /**
     * カメラID.
     *
     * @see CameraManager#getCameraIdList()
     */
    protected final String mCameraId;

    /**
     * カメラの位置.
     */
    protected final Camera2Recorder.CameraFacing mFacing;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param cameraId カメラID
     */
    protected AbstractCamera2Recorder(final @NonNull Context context, final @NonNull String cameraId) {
        super(context, NOTIFICATION_ID_BASE + cameraId.hashCode());
        mCameraId = cameraId;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mFacing = detectFacing();
    }

    /**
     * カメラの位置を判定する.
     * @return カメラの位置
     */
    private CameraFacing detectFacing() {
        try {
            int facing = Camera2Helper.getFacing(mCameraManager, mCameraId);
            switch (facing) {
                case CameraCharacteristics.LENS_FACING_BACK:
                    return CameraFacing.BACK;
                case CameraCharacteristics.LENS_FACING_FRONT:
                    return CameraFacing.FRONT;
                case CameraCharacteristics.LENS_FACING_EXTERNAL:
                    return CameraFacing.EXTERNAL;
                default:
                    return CameraFacing.UNKNOWN;
            }
        } catch (CameraAccessException e) {
            return CameraFacing.UNKNOWN;
        }
    }

    /**
     * カメラの位置.
     */
    public enum CameraFacing {
        /** スマートフォンの裏側. */
        BACK("Back"),

        /** スマートフォンの正面. */
        FRONT("Front"),

        /** 外部接続. (e.g. OTG 接続されている USB カメラ) */
        EXTERNAL("External"),

        /** 不明. */
        UNKNOWN("Unknown");

        /** カメラの位置を表現する名前. */
        private final String mName;

        /**
         * コンストラクタ.
         * @param name カメラの位置を表現する名前
         */
        CameraFacing(final String name) {
            mName = name;
        }

        /**
         * カメラの位置を表現する名前を取得する.
         * @return 名前
         */
        public String getName() {
            return mName;
        }
    }

}
