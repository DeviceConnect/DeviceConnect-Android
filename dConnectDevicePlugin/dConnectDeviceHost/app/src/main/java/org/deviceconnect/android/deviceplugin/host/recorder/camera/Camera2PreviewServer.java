package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;

/**
 * Camera2 のプレビュー配信サーバの基底クラス.
 */
abstract class Camera2PreviewServer extends AbstractPreviewServer {

    /**
     * Camera2 のイベントを通知するリスナー.
     */
    private OnEventListener mOnEventListener;

    Camera2PreviewServer(Context context, HostDeviceRecorder recorder) {
        super(context, recorder);
    }

    /**
     * カメラの再起動を要求します.
     */
    abstract void restartCamera();

    /**
     * Camera2 のイベントを通知するリスナーを設定します.
     *
     * @param lister リスナー
     */
    void setOnEventListener(OnEventListener lister) {
        mOnEventListener = lister;
    }

    /**
     * Camera2 が開始されることを通知します.
     */
    void postOnCameraStarted() {
        if (mOnEventListener != null) {
            mOnEventListener.onCameraStarted();
        }
    }

    /**
     * Camera2 が停止されたことを通知します.
     */
    void postOnCameraStopped() {
        if (mOnEventListener != null) {
            mOnEventListener.onCameraStopped();
        }
    }

    /**
     * Camera2 のイベントを通知するリスナー.
     */
    interface OnEventListener {
        /**
         * Camera2 が開始されることを通知します.
         *
         * <p>
         * Camera2 が開始されるので、他で Camera2 を使用している場合は停止処理などを行うこと。
         * </p>
         */
        void onCameraStarted();

        /**
         * Camera2 が停止されることを通知します.
         *
         * <p>
         * Camera2 のプレビューが停止したので、他で使用していた Camera2 の再開処理などを行うこと。
         * </p>
         */
        void onCameraStopped();
    }
}
