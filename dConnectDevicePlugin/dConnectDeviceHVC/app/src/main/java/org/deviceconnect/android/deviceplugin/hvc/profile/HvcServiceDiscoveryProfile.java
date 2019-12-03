/*
 HvcServiceDiscoveryProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.profile;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.hvc.HvcDeviceService;
import org.deviceconnect.android.deviceplugin.hvc.ble.BleUtils;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.service.DConnectServiceProvider;
import org.deviceconnect.message.DConnectMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * HVC DevicePlugin, Network Service Discovery Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    private static final int DISCOVERY_WAIT = 4000;
    
    private final HandlerThread mWorkerThread;
    
    /**
     * Constructor.
     * @param provider service provider
     */
    public HvcServiceDiscoveryProfile(final DConnectServiceProvider provider) {
        super(provider);
        mWorkerThread = new HandlerThread(getClass().getSimpleName() + "_" + this.hashCode());
        mWorkerThread.start();

        addApi(new GetApi() {

            @Override
            public boolean onRequest(final Intent request, final Intent response) {

                // ble os available?
                if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                    // ble not available.(result=OK, service=0)
                    setResult(response, DConnectMessage.RESULT_OK);
                    return true;
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    appendServiceList(response);
                    return true;
                } else {
                    if (BleUtils.isBLEPermission(getContext())) {
                        appendServiceList(response);
                        return true;
                    } else {
                        PermissionUtility.requestPermissions(getContext(), new Handler(mWorkerThread.getLooper()),
                            BleUtils.BLE_PERMISSIONS,
                            new PermissionUtility.PermissionRequestCallback() {
                                @Override
                                public void onSuccess() {
                                    startSearchHvcDevice();

                                    // Wait for discovered device cache list to be filled up.
                                    Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                                        appendServiceList(response);
                                        sendResponse(response);
                                    }, DISCOVERY_WAIT, TimeUnit.MILLISECONDS);
                                }
                                @NonNull
                                @Override
                                public void onFail(final String deniedPermission) {
                                    MessageUtils.setIllegalServerStateError(response,
                                        "Bluetooth LE scan requires permissions ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION.");
                                    sendResponse(response);
                                }
                            });
                        return false;
                    }
                }
            }
        });
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mWorkerThread.quit();
    }

    private void startSearchHvcDevice() {
        HvcDeviceService s = (HvcDeviceService) getContext();
        s.startSearchHvcDevice();
    }
}
