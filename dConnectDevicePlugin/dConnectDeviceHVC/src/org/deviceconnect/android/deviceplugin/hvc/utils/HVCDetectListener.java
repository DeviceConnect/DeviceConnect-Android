package org.deviceconnect.android.deviceplugin.hvc.utils;

import omron.HVC.HVC_RES;

public interface HVCDetectListener {
    void onDetectFinished(HVC_RES result);
    void onDetectFaceTimeout();
    void onDetectFaceDisconnected();
}
