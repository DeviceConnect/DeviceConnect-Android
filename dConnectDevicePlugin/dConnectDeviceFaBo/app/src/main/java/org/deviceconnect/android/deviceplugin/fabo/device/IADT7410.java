package org.deviceconnect.android.deviceplugin.fabo.device;

public interface IADT7410 {

    /**
     * ADT7410の温度センサーを開始します.
     */
    void start();

    /**
     * ADT7410の加速度センサーを停止します.
     */
    void stop();

    /**
     * リスナーを設定します.
     * @param listener リスナー
     */
    void setOnADT7410Listener(final OnADT7410Listener listener);

    /**
     * ADT7410からのデータを受け取るリスナー.
     */
    interface OnADT7410Listener {
        /**
         * 加速度センサーを受け取る.
         * @param temperature 温度
         */
        void onData(final double temperature);
    }
}
