package org.deviceconnect.android.deviceplugin.fabo.device;

/**
 * VCNL4010を操作するためのインターフェース.
 */
public interface IVCNL4010 {

    /**
     * 近距離センサーの値を取得します.
     * @param listener 通知を受け取るリスナー
     */
    void readProximity(final OnProximityListener listener);

    /**
     * 近距離センサー値の連続取得を開始します.
     * @param listener 通知を受け取るリスナー
     */
    void startProximity(final OnProximityListener listener);

    /**
     * 近距離センサー値の連続取得を停止します.
     * @param listener 通知を受け取るリスナー
     */
    void stopProximity(final OnProximityListener listener);

    /**
     * 照度センサーの値を取得します.
     */
    void readAmbientLight(final OnAmbientLightListener listener);

    /**
     * 照度センサー値の連続取得を開始します.
     */
    void startAmbientLight(final OnAmbientLightListener listener);

    /**
     * 照度センサー値の連続取得を停止します.
     */
    void stopAmbientLight(final OnAmbientLightListener listener);

    /**
     * 近距離センサー値の通知を受けるリスナー.
     */
    interface OnProximityListener {
        /**
         * 近距離センサーの開始通知を受け取ります.
         */
        void onStarted();

        /**
         * 近距離センサー値を受け取ります.
         * @param proximity 距離
         */
        void onData(final boolean proximity);

        /**
         * 近距離センサー値の取得失敗通知を受け取ります.
         * @param message エラーメッセージ
         */
        void onError(final String message);
    }

    interface OnAmbientLightListener {
        void onStarted();
        void onData(final double ambient);
        void onError(final String message);
    }
}
