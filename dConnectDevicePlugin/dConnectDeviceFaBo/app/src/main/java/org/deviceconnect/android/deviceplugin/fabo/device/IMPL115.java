package org.deviceconnect.android.deviceplugin.fabo.device;

public interface IMPL115 {
    void readAtmosphericPressure(OnAtmosphericPressureListener listener);

    interface OnAtmosphericPressureListener {
        void onData(double hpa, double temperature);
        void onError(String message);
    }

}
