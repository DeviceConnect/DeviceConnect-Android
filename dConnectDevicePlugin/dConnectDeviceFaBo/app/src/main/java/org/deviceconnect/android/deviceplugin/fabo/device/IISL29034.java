package org.deviceconnect.android.deviceplugin.fabo.device;

public interface IISL29034 {
    void read(OnAmbientLightListener lightListener);
    void startRead();
    void stopRead();

    interface OnAmbientLightListener {
        void onData(double lux);
        void onError(String message);
    }
}
