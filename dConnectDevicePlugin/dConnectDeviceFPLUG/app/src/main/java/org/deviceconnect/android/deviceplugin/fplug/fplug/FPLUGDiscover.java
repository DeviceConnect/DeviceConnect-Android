/*
 FPLUGDiscover.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.fplug;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class provides function of discovery of F-PLUG.
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGDiscover {

    public static List<BluetoothDevice> getAll() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            return null;
        }
        if (!adapter.isEnabled()) {
            return null;
        }
        List<BluetoothDevice> deviceList = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            String name = device.getName();
            if (name != null && name.contains("F-PLUG")) {
                deviceList.add(device);
            }
        }
        return deviceList;
    }

}
