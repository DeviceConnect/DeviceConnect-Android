package org.deviceconnect.android.libsrt.player.decoder;

/**
 * フレームデータをデコードするクラスのインターフェース.
 */
public interface Decoder {
    /**
     * デコーダの初期化を行います.
     */
    void onInit();

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
    void onReceived(byte[] data, int dataLength, long pts);

    /**
     * デコーダの後始末を行います.
     */
    void onReleased();

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
