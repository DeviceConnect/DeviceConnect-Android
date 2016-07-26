/*
 WebRTCDeviceService.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc;

import org.deviceconnect.android.deviceplugin.webrtc.profile.WebRTCSystemProfile;
import org.deviceconnect.android.deviceplugin.webrtc.service.WebRTCService;
import org.deviceconnect.android.deviceplugin.webrtc.util.WebRTCManager;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * WebRTC device plug-in.
 *
 * @author NTT DOCOMO, INC.
 */
public class WebRTCDeviceService extends DConnectMessageService {

    private WebRTCManager mWebRTCManager;

    @Override
    public void onCreate() {
        super.onCreate();
        EventManager.INSTANCE.setController(new MemoryCacheController());
        mWebRTCManager = new WebRTCManager((WebRTCApplication) getApplication());
        getServiceProvider().addService(new WebRTCService((WebRTCApplication) getApplication()));
    }

    @Override
    public void onDestroy() {
        if (mWebRTCManager != null) {
            mWebRTCManager.destroy();
            mWebRTCManager = null;
        }
        super.onDestroy();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new WebRTCSystemProfile();
    }

    public WebRTCManager getWebRTCManager() {
        return mWebRTCManager;
    }
}
