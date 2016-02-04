package org.deviceconnect.android.deviceplugin.theta.core.sensor;


import org.deviceconnect.android.deviceplugin.theta.utils.Quaternion;

public interface HeadTrackingListener {

    void onHeadRotated(Quaternion rotation);

}
