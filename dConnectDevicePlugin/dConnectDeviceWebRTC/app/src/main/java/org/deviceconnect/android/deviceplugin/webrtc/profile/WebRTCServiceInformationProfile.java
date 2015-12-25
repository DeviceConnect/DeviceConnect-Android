/*
 WebRTCServiceInformationProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.profile;

import org.deviceconnect.android.deviceplugin.webrtc.WebRTCApplication;
import org.deviceconnect.android.deviceplugin.webrtc.WebRTCDeviceService;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceInformationProfile;

/**
 * WebRTC Service Information Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class WebRTCServiceInformationProfile extends ServiceInformationProfile {

    /**
     * Constructor.
     *
     * @param provider Provider
     */
    public WebRTCServiceInformationProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected ConnectState getWifiState(final String serviceId) {
        if (isConnected()) {
            return ConnectState.ON;
        } else {
            return ConnectState.OFF;
        }
    }

    private boolean isConnected() {
        WebRTCDeviceService service = (WebRTCDeviceService) getContext();
        WebRTCApplication application = (WebRTCApplication) service.getApplication();
        return application.isConnected();
    }

}
