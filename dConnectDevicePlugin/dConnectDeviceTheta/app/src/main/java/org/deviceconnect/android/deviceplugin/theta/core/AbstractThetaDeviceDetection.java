package org.deviceconnect.android.deviceplugin.theta.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

abstract class AbstractThetaDeviceDetection implements ThetaDeviceDetection {

    private final List<DetectionListener> mDetectionListeners = new ArrayList<>();

    @Override
    public void registerListener(final DetectionListener listener) {
        synchronized (mDetectionListeners) {
            for (DetectionListener cache : mDetectionListeners) {
                if (cache == listener) {
                    return;
                }
            }
            mDetectionListeners.add(listener);
        }
    }

    @Override
    public void unregisterListener(final DetectionListener listener) {
        synchronized (mDetectionListeners) {
            for (Iterator<DetectionListener> it = mDetectionListeners.iterator(); it.hasNext(); ) {
                DetectionListener cache = it.next();
                if (cache == listener) {
                    it.remove();
                    return;
                }
            }
        }
    }

    void notifyOnThetaDetected(final ThetaDevice device) {
        synchronized (mDetectionListeners) {
            for (DetectionListener cache : mDetectionListeners) {
                cache.onThetaDetected(device);
            }
        }
    }

    void notifyOnThetaLost(final ThetaDevice device) {
        synchronized (mDetectionListeners) {
            for (DetectionListener cache : mDetectionListeners) {
                cache.onThetaLost(device);
            }
        }
    }
}
