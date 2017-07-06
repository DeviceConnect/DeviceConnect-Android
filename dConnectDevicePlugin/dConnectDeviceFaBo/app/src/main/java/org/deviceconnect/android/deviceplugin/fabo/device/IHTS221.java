package org.deviceconnect.android.deviceplugin.fabo.device;

/**
 * HTS221を操作するためのインターフェース.
 */
public interface IHTS221 {
    /**
     * HTS221から湿度の取得を行います.
     * @param callback 湿度のデータを通知するコールバック
     */
    void readHumidity(final OnHumidityCallback callback);

    /**
     * HTS221の温度の取得を行います.
     * @param callback 温度のデータを通知するコールバック
     */
    void readTemperature(final OnTemperatureCallback callback);

    /**
     * 湿度のデータを通知するコールバック.
     */
    interface OnHumidityCallback {
        void onHumidity(final double humidity);
        void onError(final String message);
    }

    /**
     * 温度のデータを通知するコールバック.
     */
    interface OnTemperatureCallback {
        void onTemperature(final double temperature);
        void onError(final String message);
    }
}
