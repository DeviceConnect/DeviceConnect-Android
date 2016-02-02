/*
 WebRTCServceDiscoveryProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

/**
 * WebRTCServceDiscoveryProfile Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class WebRTCServceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * Defined a plug-in id.
     */
    public static final String PLUGIN_ID = "webrtcplugin";

    /**
     * Constructor.
     *
     * @param provider an instance of {@link DConnectProfileProvider}
     */
    public WebRTCServceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        Bundle service = new Bundle();
        ServiceDiscoveryProfile.setId(service, PLUGIN_ID);
        ServiceDiscoveryProfile.setName(service, "WebRTC Service");
        ServiceDiscoveryProfile.setType(service, NetworkType.WIFI);
        ServiceDiscoveryProfile.setOnline(service, true);
        ServiceDiscoveryProfile.setConfig(service, "{apiKey:\"XXX\", domain:\"XXX\"}");
        ServiceDiscoveryProfile.setScopes(service, getProfileProvider());
        Bundle[] services = new Bundle[] {
                service
        };
        setServices(response, services);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

}
