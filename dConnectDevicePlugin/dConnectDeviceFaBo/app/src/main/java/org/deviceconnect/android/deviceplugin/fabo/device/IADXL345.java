package org.deviceconnect.android.deviceplugin.fabo.device;

/**
 * ADXL345を操作するクラス.
 */
public interface IADXL345 {

    /**
     * ADXL345の加速度センサーを開始します.
     */
    void start();

    /**
     * ADXL345の加速度センサーを停止します.
     */
    void stop();

    /**
     * リスナーを設定します.
     * @param listener リスナー
     */
    void setOnADXL345Listener(final OnADXL345Listener listener);

    /**
     * ADXL345からのデータを受け取るリスナー.
     */
    interface OnADXL345Listener {
        /**
         * 加速度センサーを受け取る.
         * @param x x軸の加速度
         * @param y y軸の加速度
         * @param z z軸の加速度
         */
        void onData(final double x, final double y, final double z);
    }
}
