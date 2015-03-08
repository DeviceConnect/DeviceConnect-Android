/*
 HvcDeviceSearchUtils.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.devicesearch;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deviceconnect.android.deviceplugin.hvc.profile.HvcConstants;

import omron.HVC.BleDeviceSearch;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

/** 
 * HVC device search utility.
 * 
 * @author NTT DOCOMO, INC.
 */
public final class HvcDeviceSearchUtils {

    /**
     * Constructor.
     */
    private HvcDeviceSearchUtils() {
        
    }
    
    /**
     * Search HVC Device.
     * @param context context
     * @return BluetoothDevice List.
     */
    public static List<BluetoothDevice> selectHvcDevices(final Context context) {
        final String regStr = HvcConstants.HVC_DEVICE_NAME_PREFIX;
        List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
        Pattern p = Pattern.compile(regStr);
        BleDeviceSearch bleSearch = new BleDeviceSearch(context, HvcConstants.DEVICE_SEARCH_WAIT_TIME);
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
}
