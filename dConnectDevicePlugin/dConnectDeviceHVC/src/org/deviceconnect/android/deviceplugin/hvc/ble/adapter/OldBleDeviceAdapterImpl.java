/*
 OldBleDeviceAdapterImpl
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.ble.adapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import org.deviceconnect.android.deviceplugin.hvc.ble.BleDeviceAdapter;
import org.deviceconnect.android.deviceplugin.hvc.ble.BleUtils;

import java.util.Set;

/**
 *
 * @author NTT DOCOMO, INC.
 */
public class OldBleDeviceAdapterImpl extends BleDeviceAdapter {

    /**
     * Bluetooth adapter.
     */
    private BluetoothAdapter mBluetoothAdapter;
    /**
     * BLE device scan callback.
     */
    private BleDeviceScanCallback mCallback;

    /**
     * Constructor.
     * @param context context
     */
    public OldBleDeviceAdapterImpl(final Context context) {
        BluetoothManager manager = BleUtils.getManager(context);
        mBluetoothAdapter = manager.getAdapter();
    }

    @Override
    public void startScan(final BleDeviceScanCallback callback) {
        mCallback = callback;
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    @Override
    public void stopScan(final BleDeviceScanCallback callback) {
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    @Override
    public BluetoothDevice getDevice(final String address) {
        return mBluetoothAdapter.getRemoteDevice(address);
    }

    @Override
    public Set<BluetoothDevice> getBondedDevices() {
        return mBluetoothAdapter.getBondedDevices();
    }

    @Override
    public boolean isEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    @Override
    public boolean checkBluetoothAddress(final String address) {
        return BluetoothAdapter.checkBluetoothAddress(address);
    }

    /**
     * BLE scan callback.
     */
    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            if (mCallback != null) {
                mCallback.onLeScan(device, rssi);
            }
        }
    };
}
