/*
 BleDeviceAdapter
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.ble;

import android.bluetooth.BluetoothDevice;

import java.util.Set;

/**
 * This abstract class is used to implements.
 * @author NTT DOCOMO, INC.
 */
public abstract class BleDeviceAdapter {
    /**
     * start scan.
     * @param callback callback
     */
    public abstract void startScan(BleDeviceScanCallback callback);

    /**
     * stop scan.
     * @param callback callback
     */
    public abstract void stopScan(BleDeviceScanCallback callback);

    /**
     * get device.
     * @param address address
     * @return device
     */
    public abstract BluetoothDevice getDevice(String address);

    /**
     * get bonded devices.
     * @return bluetooth devices
     */
    public abstract Set<BluetoothDevice> getBondedDevices();

    /**
     * check enabled.
     * @return true: enable / false: disable
     */
    public abstract boolean isEnabled();

    /**
     * check bluetooth address.
     * @param address bluetooth address
     * @return true: bluetooth address / false: not bluetooth address
     */
    public abstract boolean checkBluetoothAddress(String address);

    /**
     * BLE device scan callback interface.
     */
    public interface BleDeviceScanCallback {
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
