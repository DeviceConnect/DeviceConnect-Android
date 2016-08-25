/*
 WebRTCService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.service;

import org.deviceconnect.android.deviceplugin.webrtc.WebRTCApplication;
import org.deviceconnect.android.deviceplugin.webrtc.profile.WebRTCVideoChatProfile;
import org.deviceconnect.android.service.DConnectService;

/**
 * WebRTC Service.
 *
 * @author NTT DOCOMO, INC.
 */
public class WebRTCService extends DConnectService {

    /**
     * Defined a plug-in id.
     */
    public static final String PLUGIN_ID = "webrtcplugin";

    private final WebRTCApplication mApplication;

    public WebRTCService(final WebRTCApplication application) {
        super(PLUGIN_ID);
        if (application == null) {
            throw new IllegalArgumentException("application is null.");
        }
        mApplication = application;
        setName("WebRTC Service");
        setNetworkType(NetworkType.WIFI);
        setOnline(true);
        setConfig("{apiKey:\"XXX\", domain:\"XXX\"}");

        addProfile(new WebRTCVideoChatProfile());
    }

    @Override
    public boolean isOnline() {
        return mApplication.isConnected();
    }
}
