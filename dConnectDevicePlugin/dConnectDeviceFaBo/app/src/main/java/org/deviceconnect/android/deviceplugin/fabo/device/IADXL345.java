package org.deviceconnect.android.deviceplugin.fabo.device;

/**
 * ADXL345を操作するためのインターフェース.
 */
public interface IADXL345 {

    /**
     * ADXL345の加速度を取得します.
     * @param listener 加速度の通知を受けるリスナー
     */
    void read(final OnADXL345Listener listener);

    /**
     * ADXL345の加速度センサーを開始します.
     * @param listener 加速度の通知を受けるリスナー
     */
    void startRead(final OnADXL345Listener listener);

    /**
     * ADXL345の加速度センサーを停止します.
     * @param listener 加速度の通知を受けるリスナー
     */
    void stopRead(final OnADXL345Listener listener);

    /**
     * ADXL345からのデータを受け取るリスナー.
     */
    interface OnADXL345Listener {
        /**
         * 加速度センサー開始成功通知を受け取ります.
         */
        void onStarted();

        /**
         * 加速度センサーを受け取ります.
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
