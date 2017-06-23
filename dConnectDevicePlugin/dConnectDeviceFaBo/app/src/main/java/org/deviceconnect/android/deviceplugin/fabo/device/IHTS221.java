package org.deviceconnect.android.deviceplugin.fabo.device;

/**
 * HTS221を操作するクラス.
 */
public interface IHTS221 {
    void readHumidity(OnHumidityCallback callback);
    void readTemperature(TemperatureCallback callback);

    interface OnHumidityCallback {
        void onHumidity(double humidity);
    }

    interface TemperatureCallback {
        void onTemperature(double temperature);
    }
}
