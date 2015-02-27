/*
 HvcServiceDiscoveryProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import omron.HVC.BleDeviceSearch;

import org.deviceconnect.android.deviceplugin.hvc.comm.HvcCommManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

/**
 * HVC DevicePlugin, Network Service Discovery Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * HVC device name prefix.
     */
    protected final static String HVC_DEVICE_NAME_PREFIX = "OMRON_HVC.*|omron_hvc.*";
    
    
    @Override
    public boolean onGetServices(final Intent request, final Intent response) {
        
        // ble os available?
        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // ble not available.(result=OK, service=0)
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
        
        Thread hvcThread = new Thread() {
            @Override
            public void run() {
                List<BluetoothDevice> devices = selectHvcDevices(HVC_DEVICE_NAME_PREFIX);
                
                // store devices.
                HvcCommManager.storeDevices(devices);
                
                // set response.
                List<Bundle> services = new ArrayList<Bundle>();
                for (BluetoothDevice device : devices) {
                    services.add(toBundle(device));
                }
                setResult(response, DConnectMessage.RESULT_OK);
                setServices(response, services);
                getContext().sendBroadcast(response);
            }
        };
        hvcThread.start();
        
        // Since returning the response asynchronously, it returns false.
        return false;
    }

    /**
     * Search HVC Device.
     * @param regStr HVC device name matching string.
     * @return BluetoothDevice List.
     */
    private List<BluetoothDevice> selectHvcDevices(final String regStr) {
        List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
        Pattern p = Pattern.compile(regStr);
        BleDeviceSearch bleSearch = new BleDeviceSearch(getContext());
        List<BluetoothDevice> deviceList  = bleSearch.getDevices();
        for (BluetoothDevice bluetoothDevice : deviceList) {
            // Generate pattern to determine
            Matcher m = p.matcher(bluetoothDevice.getName());
            if (m.find()) {
                devices.add(bluetoothDevice);
            }
        }
        return devices;
    }

    /**
     * Returns by storing the {@link Bundle} status of HVC. 
     * 
     * @param foundDevice Found device list
     * @return {@link Bundle}instance
     */
    public Bundle toBundle(final BluetoothDevice foundDevice) {

        String address = foundDevice.getAddress();
        String serviceId = address.replace(":", "").toLowerCase(Locale.ENGLISH);
        Bundle result = new Bundle();
        result.putString(ServiceDiscoveryProfile.PARAM_ID, serviceId);
        result.putString(ServiceDiscoveryProfile.PARAM_NAME, foundDevice.getName());
        result.putString(ServiceDiscoveryProfile.PARAM_TYPE, NetworkType.BLE.getValue());
        result.putBoolean(ServiceDiscoveryProfile.PARAM_ONLINE, true);

        return result;
    }
}
