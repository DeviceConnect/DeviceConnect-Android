package org.deviceconnect.android.deviceplugin.hvc.comm;

import omron.HVC.HVC_RES;

public interface HvcDetectListener {
    void onDetectFinished(HVC_RES result);
    void onDetectFaceTimeout();
    void onDetectFaceDisconnected();
}
