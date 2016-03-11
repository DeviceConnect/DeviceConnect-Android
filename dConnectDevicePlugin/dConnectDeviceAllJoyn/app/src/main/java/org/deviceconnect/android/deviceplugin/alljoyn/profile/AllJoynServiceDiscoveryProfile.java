package org.deviceconnect.android.deviceplugin.alljoyn.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.alljoyn.AllJoynDeviceApplication;
import org.deviceconnect.android.deviceplugin.alljoyn.AllJoynServiceEntity;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.LinkedList;
import java.util.List;

/**
 * System Discovery profile for AllJoyn.
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    public AllJoynServiceDiscoveryProfile(DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        AllJoynDeviceApplication app =
                (AllJoynDeviceApplication) getContext().getApplicationContext();

        app.performDiscovery();
        List<Bundle> services = new LinkedList<>();
        for (AllJoynServiceEntity serviceEntity : app.getDiscoveredAlljoynServices().values()) {
            if (serviceEntity.serviceName.indexOf("LuminaireC") != -1) {
                continue;
            }
            Bundle service = new Bundle();
            ServiceDiscoveryProfile.setId(service, serviceEntity.appId);
            ServiceDiscoveryProfile.setName(service, serviceEntity.serviceName);
            ServiceDiscoveryProfile.setScopes(service, getProfileProvider());
            ServiceDiscoveryProfile.setOnline(service, true);
            ServiceDiscoveryProfile.setType(service, NetworkType.WIFI);
            services.add(service);
        }
        setServices(response, services);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

}
