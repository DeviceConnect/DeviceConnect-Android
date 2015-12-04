/*
 HvcServiceDiscoveryProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.profile;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.hvc.HvcDeviceService;
import org.deviceconnect.android.deviceplugin.hvc.ble.BleUtils;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
     * @param provider profile provider
     */
    public HvcServiceDiscoveryProfile(final DConnectProfileProvider provider) {
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
    public boolean onGetServices(final Intent request, final Intent response) {
        
        // ble os available?
        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // ble not available.(result=OK, service=0)
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }

        final Runnable perform = new Runnable() {
            @Override
            public void run() {
                // get device list.
                List<BluetoothDevice> devices = ((HvcDeviceService) getContext()).getHvcDeviceList();
                // set response.
                List<Bundle> services = new ArrayList<Bundle>();
                for (BluetoothDevice device : devices) {
                    services.add(toBundle(device));
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
                                startSearchHvcDevice();

                                // Wait for discovered device cache list to be filled up.
                                Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        perform.run();
                                        sendResponse(response);
                                    }
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

    /**
     * Returns by storing the {@link Bundle} status of HVC. 
     * 
     * @param foundDevice Found device list
     * @return {@link Bundle}instance
     */
    private Bundle toBundle(final BluetoothDevice foundDevice) {
        String address = foundDevice.getAddress();
        String serviceId = address.replace(":", "").toLowerCase(Locale.ENGLISH);
        Bundle result = new Bundle();
        result.putString(ServiceDiscoveryProfile.PARAM_ID, serviceId);
        result.putString(ServiceDiscoveryProfile.PARAM_NAME, foundDevice.getName());
        result.putString(ServiceDiscoveryProfile.PARAM_TYPE, NetworkType.BLE.getValue());
        result.putBoolean(ServiceDiscoveryProfile.PARAM_ONLINE, true);
        setScopes(result, getProfileProvider());
        return result;
    }

    private void startSearchHvcDevice() {
        HvcDeviceService s = (HvcDeviceService) getContext();
        s.startSearchHvcDevice();
    }
}
