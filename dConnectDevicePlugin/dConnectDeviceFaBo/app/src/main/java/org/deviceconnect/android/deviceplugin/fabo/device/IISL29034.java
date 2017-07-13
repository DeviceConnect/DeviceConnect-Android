package org.deviceconnect.android.deviceplugin.fabo.device;

/**
 * ISL29034を操作するためのインターフェース.
 */
public interface IISL29034 {
    /**
     * 照度センサー値を取得します.
     * @param lightListener 照度センサー値を受け取るリスナー
     */
    void read(final OnAmbientLightListener lightListener);

    /**
     * 照度センサー値の連続取得を開始します.
     * @param lightListener 照度センサー値を受け取るリスナー
     */
    void startRead(final OnAmbientLightListener lightListener);

    /**
     * 照度センサー値の連続取得を停止します.
     * @param lightListener 照度センサー値を受け取るリスナー
     */
    void stopRead(final OnAmbientLightListener lightListener);

    /**
     * 照度センサー値の通知を行うリスナー.
     */
    interface OnAmbientLightListener {
        /**
         * 照度センサーの開始成功通知を受け取ります.
         */
        void onStarted();

        /**
         * 照度センサー値の通知を受け取ります.
         * @param lux 照度
         */
        void onData(final double lux);

        /**
         * 照度センサー値の取得失敗通知を受け取ります.
         * @param message エラーメッセージ
         */
        void onError(final String message);
    }
}
