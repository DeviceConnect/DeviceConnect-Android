package org.deviceconnect.android.deviceplugin.hvcc2w.profile;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.deviceconnect.android.deviceplugin.hvcc2w.BuildConfig;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.HVCManager;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.HVCCameraInfo;
import org.deviceconnect.android.deviceplugin.hvcc2w.service.HVCC2WService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceProvider;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * HVCC2W Service Discovery Profile.
 *
 * @author NTT DOCOMO, INC.
 */

public class HVCC2WServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * Constructor.
     *
     * @param provider an instance of {@link DConnectServiceProvider}
     */
    public HVCC2WServiceDiscoveryProfile(final DConnectServiceProvider provider) {
        super(provider);
        addApi(mServiceDiscoveryApi);
    }

    private final DConnectApi mServiceDiscoveryApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            if (!isNetwork()) {
                return true;
            }
            HVCManager.INSTANCE.getCameraList(new HVCManager.ResponseListener() {
                @Override
                public void onReceived(String json) {

                    final Map<String, HVCCameraInfo> devices = HVCManager.INSTANCE.getHVCDevices();
                    final CountDownLatch countDownLatch = new CountDownLatch(devices.size());
                    final List<DConnectService> disappeared = getServiceProvider().getServiceList();
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

                                // 検出されたデバイスについてのサービスを登録.
                                DConnectService hvcService = getServiceProvider().getService(camera.getID());
                                if (hvcService == null) {
                                    hvcService = new HVCC2WService(camera);
                                    getServiceProvider().addService(hvcService);
                                } else {
                                    for (int j = 0; j < disappeared.size(); j++) {
                                        DConnectService cache = disappeared.get(j);
                                        if (cache.getId().equals(hvcService.getId())) {
                                            disappeared.remove(j);
                                            break;
                                        }
                                    }
                                }
                                // 検出されたデバイスはオンライン状態とみなす.
                                hvcService.setOnline(true);

                                countDownLatch.countDown();
                            }
                        });
                    }
                    try {
                        countDownLatch.await(3000, TimeUnit.MILLISECONDS);

                        // 消失したデバイスについてはオフライン状態とみなす.
                        for (DConnectService service : disappeared) {
                            service.setOnline(false);
                        }

                        // レスポンス作成.
                        appendServiceList(response);
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
    };

    private boolean isNetwork(){
        ConnectivityManager cm =  (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
}
