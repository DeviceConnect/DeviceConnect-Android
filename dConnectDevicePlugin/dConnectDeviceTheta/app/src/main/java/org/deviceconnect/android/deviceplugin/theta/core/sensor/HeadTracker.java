package org.deviceconnect.android.deviceplugin.theta.core.sensor;


public interface HeadTracker {

    void start();

    void stop();

    void registerTrackingListener(HeadTrackingListener listener);

    void unregisterTrackingListener(HeadTrackingListener listener);

}
