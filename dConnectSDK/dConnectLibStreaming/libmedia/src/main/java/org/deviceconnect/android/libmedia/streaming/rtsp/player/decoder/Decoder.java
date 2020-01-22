package org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder;

import org.deviceconnect.android.libmedia.streaming.sdp.MediaDescription;

/**
 * フレームデータをデコードするクラスのインターフェース.
 */
public interface Decoder {
    /**
     * デコーダの初期化を行います.
     */
    void onInit(MediaDescription md);

    /**
     * デコーダで発生したイベントを通知するリスナーを設定します.
     * @param listener リスナー
     */
    void setErrorCallback(ErrorCallback listener);

    /**
     * 送られてくるフレームデータを受け取ります.
     *
     * @param data フレームデータ
     * @param dataLength データサイズ
     */
    void onRtpReceived(MediaDescription md, byte[] data, int dataLength);

    /**
     * 送られてくるフレームデータを受け取ります.
     *
     * @param data フレームデータ
     * @param dataLength データサイズ
     */
    void onRtcpReceived(MediaDescription md, byte[] data, int dataLength);

    /**
     * デコーダの後始末を行います.
     */
    void onRelease();

    /**
     * デコーダのイベントを通知するリスナー.
     */
    interface ErrorCallback {
        /**
         * デコーダでエラーが発生したことを通知します.
         *
         * @param e エラー原因
         */
        void onError(Exception e);
    }
}
