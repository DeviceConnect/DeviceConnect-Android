package org.deviceconnect.android.deviceplugin.fabo.device;

/**
 * ADXL345を操作するクラス.
 */
public interface IADXL345 {

    void read(final OnADXL345Listener listener);

    /**
     * ADXL345の加速度センサーを開始します.
     */
    void startRead(final OnADXL345Listener listener);

    /**
     * ADXL345の加速度センサーを停止します.
     */
    void stopRead(final OnADXL345Listener listener);

    /**
     * ADXL345からのデータを受け取るリスナー.
     */
    interface OnADXL345Listener {
        void onStarted();

        /**
         * 加速度センサーを受け取る.
         * @param x x軸の加速度
         * @param y y軸の加速度
         * @param z z軸の加速度
         */
        void onData(final double x, final double y, final double z);

        /**
         * 加速度センサーでエラーが発生したことを受け取ります.
         * @param message エラーメッセージ
         */
        void onError(final String message);
    }
}
