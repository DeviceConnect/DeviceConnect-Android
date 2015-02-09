/*
 OldBleDeviceAdapterImpl
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.ble.adapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import org.deviceconnect.android.deviceplugin.heartrate.ble.BleDeviceAdapter;
import org.deviceconnect.android.deviceplugin.heartrate.ble.BleUtils;

/**
 *
 * @author NTT DOCOMO, INC.
 */
public class OldBleDeviceAdapterImpl extends BleDeviceAdapter {

    private BluetoothAdapter mBluetoothAdapter;
    private BleDeviceScanCallback mCallback;

    public OldBleDeviceAdapterImpl(Context context) {
        BluetoothManager manager = BleUtils.getManager(context);
        mBluetoothAdapter = manager.getAdapter();
    }

    @Override
    public void startScan(BleDeviceScanCallback callback) {
        mCallback = callback;
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    @Override
    public void stopScan(BleDeviceScanCallback callback) {
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    @Override
    public BluetoothDevice getDevice(String address) {
        return mBluetoothAdapter.getRemoteDevice(address);
    }

    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            if (mCallback != null) {
                mCallback.onLeScan(device, rssi);
            }
        }
    };
}
