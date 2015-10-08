/*
 HeartRateServiceDiscoveryProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.profile;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.heartrate.HeartRateApplication;
import org.deviceconnect.android.deviceplugin.heartrate.HeartRateDeviceService;
import org.deviceconnect.android.deviceplugin.heartrate.HeartRateManager;
import org.deviceconnect.android.deviceplugin.heartrate.ble.BleUtils;
import org.deviceconnect.android.deviceplugin.heartrate.data.HeartRateDevice;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Implement ServiceDiscoveryProfile.
 * @author NTT DOCOMO, INC.
 */
public class HeartRateServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    private static final int DISCOVERY_WAIT = 6000;

    private final HandlerThread mWorkerThread;

    /**
     * コンストラクタ.
     * @param provider プロファイルプロバイダ
     */
    public HeartRateServiceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
        mWorkerThread = new HandlerThread(getClass().getSimpleName() + "_" + this.hashCode());
        mWorkerThread.start();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mWorkerThread.quit();
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        if (!BleUtils.isBLESupported(getContext())) {
            mLogger.warning("BLE not supported.");
            List<Bundle> services = new ArrayList<>();
            setResult(response, DConnectMessage.RESULT_OK);
            setServices(response, services);
            return true;
        }

        final Runnable perform = new Runnable() {
            @Override
            public void run() {
                List<Bundle> services = new ArrayList<>();
                List<HeartRateDevice> devices = getManager().getConnectedDevices();
                synchronized (devices) {
                    for (HeartRateDevice device : devices) {
                        Bundle service = new Bundle();
                        service.putString(PARAM_ID, device.getAddress());
                        service.putString(PARAM_NAME, device.getName());
                        service.putString(PARAM_TYPE, NetworkType.BLE.getValue());
                        service.putBoolean(PARAM_ONLINE, true);
                        service.putString(PARAM_CONFIG, "");
                        setScopes(service, getProfileProvider());
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
                                // Wait for discovered device cache list to be filled up.
                                Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        perform.run();
                                        getContext().sendBroadcast(response);
                                    }
                                }, DISCOVERY_WAIT, TimeUnit.MILLISECONDS);
                            }

                            @Override
                            public void onFail(@NonNull String deniedPermission) {
                                MessageUtils.setIllegalServerStateError(response,
                                        "Bluetooth LE scan requires permissions ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION.");
                                getContext().sendBroadcast(response);
                            }
                        });
                return false;
            }
        }
    }

    @Override
    protected boolean onPutOnServiceChange(final Intent request, final Intent response,
                                           final String serviceId, final String sessionKey) {
        return super.onPutOnServiceChange(request, response, serviceId, sessionKey);
    }

    @Override
    protected boolean onDeleteOnServiceChange(final Intent request, final Intent response,
                                              final String serviceId, final String sessionKey) {
        return super.onDeleteOnServiceChange(request, response, serviceId, sessionKey);
    }

    /**
     * Gets a instance of HeartRateManager.
     * @return instance of HeartRateManager
     */
    private HeartRateManager getManager() {
        HeartRateDeviceService service = (HeartRateDeviceService) getContext();
        HeartRateApplication app = (HeartRateApplication) service.getApplication();
        return app.getHeartRateManager();
    }
}
