/*
 BleDeviceAdapter
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.ble;

import android.bluetooth.BluetoothDevice;

import java.util.Set;
import java.util.UUID;

/**
 * This abstract class is used to implements.
 * @author NTT DOCOMO, INC.
 */
public abstract class BleDeviceAdapter {
    protected final UUID[] mServiceUuids = {
            UUID.fromString(BleUtils.SERVICE_HEART_RATE_SERVICE)
    };
    public abstract void startScan(BleDeviceScanCallback callback);
    public abstract void stopScan(BleDeviceScanCallback callback);
    public abstract BluetoothDevice getDevice(String address);
    public abstract Set<BluetoothDevice> getBondedDevices();
    public abstract boolean isEnabled();
    public abstract boolean checkBluetoothAddress(String address);
    /**
     * BLE device scan callback interface.
     */
    public static interface BleDeviceScanCallback {
        /**
         * BLE scan listener.
         * @param device device
         * @param rssi rssi
         */
        void onLeScan(BluetoothDevice device, int rssi);

        /**
         * Called if scan failed.
         */
        void onFail();
    }
}
