/*
 NewBleDeviceAdapterImpl
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.ble.adapter;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;

import org.deviceconnect.android.deviceplugin.heartrate.ble.BleDeviceAdapter;
import org.deviceconnect.android.deviceplugin.heartrate.ble.BleUtils;

import java.util.List;

/**
 *
 * @author NTT DOCOMO, INC.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class NewBleDeviceAdapterImpl extends BleDeviceAdapter {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBleScanner;
    private BleDeviceScanCallback mCallback;

    public NewBleDeviceAdapterImpl(Context context) {
        BluetoothManager manager = BleUtils.getManager(context);
        mBluetoothAdapter = manager.getAdapter();
        mBleScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    @Override
    public void startScan(BleDeviceScanCallback callback) {
        mCallback = callback;
        mBleScanner.startScan(mScanCallback);
    }

    @Override
    public void stopScan(BleDeviceScanCallback callback) {
        mBleScanner.stopScan(mScanCallback);
    }

    @Override
    public BluetoothDevice getDevice(String address) {
        return mBluetoothAdapter.getRemoteDevice(address);
    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (mCallback != null) {
                mCallback.onLeScan(result.getDevice(), result.getRssi());
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
        }

        @Override
        public void onScanFailed(int errorCode) {
        }
    };
}
