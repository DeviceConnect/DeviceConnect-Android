/*
 WebRTCDeviceService.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc;

import org.deviceconnect.android.deviceplugin.webrtc.profile.WebRTCServceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.webrtc.profile.WebRTCServiceInformationProfile;
import org.deviceconnect.android.deviceplugin.webrtc.profile.WebRTCSystemProfile;
import org.deviceconnect.android.deviceplugin.webrtc.profile.WebRTCVideoChatProfile;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * WebRTC device plug-in.
 *
 * @author NTT DOCOMO, INC.
 */
public class WebRTCDeviceService extends DConnectMessageService {

    @Override
    public void onCreate() {
        super.onCreate();
        EventManager.INSTANCE.setController(new MemoryCacheController());
        addProfile(new WebRTCVideoChatProfile());
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new WebRTCSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new WebRTCServiceInformationProfile(this);
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new WebRTCServceDiscoveryProfile(this);
    }

}
