package org.deviceconnect.android.deviceplugin.fabo.device;

public interface IADT7410 {

    void read(final OnADT7410Listener listener);

    /**
     * ADT7410の温度センサーを開始します.
     */
    void startRead(final OnADT7410Listener listener);

    /**
     * ADT7410の温度センサーを停止します.
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
