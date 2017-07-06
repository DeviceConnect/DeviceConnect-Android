package org.deviceconnect.android.deviceplugin.fabo.device;

/**
 * ADT7410を操作するためのインターフェース.
 */
public interface IADT7410 {

    /**
     * ADT7410の温度センサーを取得します.
     * @param listener 取得した温度を通知するリスナー
     */
    void read(final OnADT7410Listener listener);

    /**
     * ADT7410の温度センサーを開始します.
     * @param listener 取得した温度を通知するリスナー
     */
    void startRead(final OnADT7410Listener listener);

    /**
     * ADT7410の温度センサーを停止します.
     * @param listener 取得した温度を通知するリスナー
     */
    void stopRead(final OnADT7410Listener listener);

    /**
     * ADT7410からのデータを受け取るリスナー.
     */
    interface OnADT7410Listener {
        /**
         * 温度センサー開始成功通知を受け取ります.
         */
        void onStarted();

        /**
         * 温度センサーを受け取ります.
         * @param temperature 温度
         */
        void onData(final double temperature);

        /**
         * 温度センサーでエラーが発生したことを受け取ります.
         * @param message
         */
        void onError(final String message);
    }
}
