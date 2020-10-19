package org.deviceconnect.android.deviceplugin.theta.core.preview.camera;


import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.preview.AbstractPreviewServer;

/**
 * Thetaのカメラ のプレビュー配信サーバの基底クラス.
 */
abstract class ThetaCameraPreviewServer extends AbstractPreviewServer {
    /**
     * プレビュー再生を行うレコーダ.
     */
    private ThetaDevice mThetaMediaRecorder;

    ThetaCameraPreviewServer(ThetaDevice recorder) {
        mThetaMediaRecorder = recorder;
    }

    /**
     * カメラの再起動を要求します.
     */
    abstract void restartCamera();

    /**
     * プレビューを表示するレコーダー.
     *
     * @return レコーダー
     */
    public ThetaDevice getRecorder() {
        return mThetaMediaRecorder;
    }
}
