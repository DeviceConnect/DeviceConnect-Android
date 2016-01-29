package org.deviceconnect.android.deviceplugin.hvcp.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.hvcp.BuildConfig;
import org.deviceconnect.android.deviceplugin.hvcp.manager.HVCManager;
import org.deviceconnect.android.deviceplugin.hvcp.manager.data.HVCCameraInfo;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.Map;

/**
 * HVCC2W Service Discovery Profile.
 *
 * @author NTT DOCOMO, INC.
 */

public class HVCPServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * Constructor.
     *
     * @param provider an instance of {@link DConnectProfileProvider}
     */
    public HVCPServiceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        Map<String, HVCCameraInfo> devices = HVCManager.INSTANCE.getHVCDevices();
        Bundle[] services = new Bundle[devices.size()];
        int index = 0;
        for (String key : devices.keySet()) {
            final HVCCameraInfo camera = devices.get(key);
            Bundle service = new Bundle();
            if (BuildConfig.DEBUG) {
                Log.d("ABC", "id:" + camera.getID());
            }
            setId(service, camera.getID());
            setName(service, camera.getName());
            setType(service, NetworkType.UNKNOWN);
            setOnline(service, true);
            setScopes(service, getProfileProvider());
            services[index++] = service;
        }
        setServices(response, services);

        DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);

        return true;
    }

}
