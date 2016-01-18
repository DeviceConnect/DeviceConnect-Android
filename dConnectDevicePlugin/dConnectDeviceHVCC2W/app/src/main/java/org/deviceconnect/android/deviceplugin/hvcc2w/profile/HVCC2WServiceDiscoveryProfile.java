package org.deviceconnect.android.deviceplugin.hvcc2w.profile;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.hvcc2w.manager.HVCManager;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.HVCCameraInfo;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.Map;

/**
 * HVCC2W Service Discovery Profile.
 *
 * @author NTT DOCOMO, INC.
 */

public class HVCC2WServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * Constructor.
     *
     * @param provider an instance of {@link DConnectProfileProvider}
     */
    public HVCC2WServiceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        if (!isNetwork()) {
            return true;
        }
        HVCManager.INSTANCE.getCameraList(new HVCManager.ResponseListener() {
            @Override
            public void onReceived(String json) {
                Map<String, HVCCameraInfo> devices = HVCManager.INSTANCE.getHVCDevices();

                Bundle[] services = new Bundle[devices.size()];
                int index = 0;
                for (String key : devices.keySet()) {
                    HVCCameraInfo camera = devices.get(key);
                    Bundle service = new Bundle();
                    ServiceDiscoveryProfile.setId(service, camera.getID());
                    ServiceDiscoveryProfile.setName(service, camera.getName());
                    ServiceDiscoveryProfile.setType(service, NetworkType.WIFI);
                    ServiceDiscoveryProfile.setOnline(service, true);
                    ServiceDiscoveryProfile.setScopes(service, getProfileProvider());
                    services[index++] = service;
                }

                setServices(response, services);
                setResult(response, DConnectMessage.RESULT_OK);
                sendResponse(response);
            }
        });
        return false;
    }

    private boolean isNetwork(){
        ConnectivityManager cm =  (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null){
            return info.isConnected();
        } else {
            return false;
        }
    }
}
