package org.deviceconnect.android.deviceplugin.theta.profile;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceService;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Theta Service Discovery Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * Constructor.
     * @param provider an instance of {@link DConnectProfileProvider}
     */
    public ThetaServiceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        return ((ThetaDeviceService) getContext()).searchDevice(request, response);
    }

}
