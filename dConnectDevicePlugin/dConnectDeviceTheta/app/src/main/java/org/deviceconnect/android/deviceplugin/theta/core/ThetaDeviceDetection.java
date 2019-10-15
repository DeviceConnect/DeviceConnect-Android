package org.deviceconnect.android.deviceplugin.theta.core;

import android.content.Context;

import java.util.List;

interface ThetaDeviceDetection {

    void registerListener(DetectionListener listener);

    void unregisterListener(DetectionListener listener);

    void start(Context context);

    void stop(Context context);

    List<ThetaDevice> getDetectedDevices();

    interface DetectionListener {

        void onThetaDetected(ThetaDevice device);

        void onThetaLost(ThetaDevice device);

    }

}
