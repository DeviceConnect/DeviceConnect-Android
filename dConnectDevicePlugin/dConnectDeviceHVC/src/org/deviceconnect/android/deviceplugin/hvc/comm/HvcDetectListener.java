package org.deviceconnect.android.deviceplugin.hvc.comm;

import omron.HVC.HVC_RES;

public interface HvcDetectListener {
    void onDetectFinished(HVC_RES result);
    void onDetectFaceDisconnected();
    void onConnectError(int status);
    void onRequestDetectError(int status);
    void onDetectError(int status);
}
