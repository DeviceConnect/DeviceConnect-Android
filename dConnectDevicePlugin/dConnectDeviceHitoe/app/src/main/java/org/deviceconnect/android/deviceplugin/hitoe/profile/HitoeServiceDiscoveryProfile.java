/*
 HitoeServiceDiscoveryProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.profile;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.hitoe.HitoeApplication;
import org.deviceconnect.android.deviceplugin.hitoe.HitoeDeviceService;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.util.BleUtils;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.service.DConnectServiceProvider;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Implement ServiceDiscoveryProfile.
 * @author NTT DOCOMO, INC.
 */
public class HitoeServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    private final HandlerThread mWorkerThread;

    /**
     * コンストラクタ.
     * @param provider プロファイルプロバイダ
     */
    public HitoeServiceDiscoveryProfile(final DConnectServiceProvider provider) {
        super(provider);
        mWorkerThread = new HandlerThread(getClass().getSimpleName() + "_" + this.hashCode());
        mWorkerThread.start();
        addApi(mServiceDiscoveryApi);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mWorkerThread.quit();
    }
    private final DConnectApi mServiceDiscoveryApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            if (!BleUtils.isBLESupported(getContext())) {
                List<Bundle> services = new ArrayList<>();
                setResult(response, DConnectMessage.RESULT_OK);
                setServices(response, services);
                return true;
            }

            final Runnable perform = new Runnable() {
                @Override
                public void run() {
                    List<Bundle> services = new ArrayList<>();
                    List<HitoeDevice> devices = getManager().getRegisterDevices();
                    synchronized (devices) {
                        for (HitoeDevice device : devices) {
                            Bundle service = new Bundle();
                            service.putString(PARAM_ID, device.getId());
                            service.putString(PARAM_NAME, device.getName());
                            service.putString(PARAM_TYPE, NetworkType.BLE.getValue());
                            service.putBoolean(PARAM_ONLINE, device.isRegisterFlag());
                            service.putString(PARAM_CONFIG, "");
                            services.add(service);
                        }
                    }
                    setResult(response, DConnectMessage.RESULT_OK);
                    setServices(response, services);
                }
            };

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                perform.run();
                return true;
            } else {
                if (BleUtils.isBLEPermission(getContext())) {
                    perform.run();
                    return true;
                } else {
                    PermissionUtility.requestPermissions(getContext(), new Handler(mWorkerThread.getLooper()),
                            BleUtils.BLE_PERMISSIONS,
                            new PermissionUtility.PermissionRequestCallback() {
                                @Override
                                public void onSuccess() {
                                    perform.run();
                                    sendResponses(response);
                                }

                                @Override
                                public void onFail(@NonNull String deniedPermission) {
                                    MessageUtils.setIllegalServerStateError(response,
                                            "Bluetooth LE scan requires permissions ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION.");
                                    sendResponses(response);
                                }
                            });
                    return false;
                }
            }
        }
    };

    private void sendResponses(final Intent response) {
        DConnectMessageService s = (DConnectMessageService) getContext();
        s.sendResponse(response);
    }

    /**
     * Gets a instance of HitoeManager.
     *
     * @return {@link HitoeManager}, or null on error
     */
    private HitoeManager getManager() {
        HitoeDeviceService service = (HitoeDeviceService) getContext();
        if (service == null) {
            return null;
        }
        HitoeApplication app = (HitoeApplication) service.getApplication();
        if (app == null) {
            return null;
        }
        return app.getHitoeManager();
    }

}
