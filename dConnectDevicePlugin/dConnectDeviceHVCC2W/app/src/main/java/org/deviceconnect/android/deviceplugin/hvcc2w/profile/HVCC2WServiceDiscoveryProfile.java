package org.deviceconnect.android.deviceplugin.hvcc2w.profile;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.hvcc2w.BuildConfig;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.HVCManager;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.HVCCameraInfo;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

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

                final Map<String, HVCCameraInfo> devices = HVCManager.INSTANCE.getHVCDevices();
                final Bundle[] services = new Bundle[devices.size()];
                final CountDownLatch countDownLatch = new CountDownLatch(devices.size());
                final int[] index = new int[1];
                index[0] = 0;
                for (String key : devices.keySet()) {
                    final HVCCameraInfo camera = devices.get(key);
                    HVCManager.INSTANCE.setCamera(camera.getID(), new HVCManager.ResponseListener() {
                        @Override
                        public void onReceived(String json) {
                            if (json == null) {
                                countDownLatch.countDown();
                                return;
                            }
                            try {
                                JSONObject result = new JSONObject(json);
                                if (result.getInt("result") != 1) {
                                    countDownLatch.countDown();
                                    return;
                                }
                            } catch (JSONException e) {
                                if (BuildConfig.DEBUG) {
                                    e.printStackTrace();
                                }
                                countDownLatch.countDown();
                                return;
                            }

                            Bundle service = new Bundle();
                            ServiceDiscoveryProfile.setId(service, camera.getID());
                            ServiceDiscoveryProfile.setName(service, camera.getName());
                            ServiceDiscoveryProfile.setType(service, NetworkType.WIFI);
                            ServiceDiscoveryProfile.setOnline(service, true);
                            ServiceDiscoveryProfile.setScopes(service, getProfileProvider());
                            services[index[0]++] = service;
                            countDownLatch.countDown();
                        }
                    });
                }
                try {
                    countDownLatch.await();
                    if (services[0] != null && services.length > 0) {
                        setServices(response, services);
                    }
                    setResult(response, DConnectMessage.RESULT_OK);
                } catch (InterruptedException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                }
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
