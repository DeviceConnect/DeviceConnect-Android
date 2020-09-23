/*
 NewBleDeviceAdapterImpl
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.ble.adapter;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;

import org.deviceconnect.android.deviceplugin.midi.ble.BleDeviceAdapter;
import org.deviceconnect.android.deviceplugin.midi.ble.BleUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author NTT DOCOMO, INC.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class NewBleDeviceAdapterImpl extends BleDeviceAdapter {

    private final Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBleScanner;
    private BleDeviceScanCallback mCallback;

    public NewBleDeviceAdapterImpl(final Context context) {
        mContext = context;
        BluetoothManager manager = BleUtils.getManager(context);
        mBluetoothAdapter = manager.getAdapter();
        mBleScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    @Override
    public void startScan(final BleDeviceScanCallback callback) {
        mCallback = callback;

        List<ScanFilter> filters = new ArrayList<ScanFilter>();
        if (mServiceUuids != null && mServiceUuids.length > 0) {
            for (UUID uuid : mServiceUuids) {
                ScanFilter filter = new ScanFilter.Builder().setServiceUuid(
                        new ParcelUuid(uuid)).build();
                filters.add(filter);
            }
        }
        ScanSettings.Builder builder = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setMatchMode(ScanSettings.MATCH_MODE_STICKY);
        }
        ScanSettings settings = builder.build();

        mBleScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBleScanner != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                mBleScanner.startScan(filters, settings, mScanCallback);
            } else {
                // Unless required permissions were acquired, scan does not start.
                if (BleUtils.isBLEPermission(mContext)) {
                    mBleScanner.startScan(filters, settings, mScanCallback);
                }
            }
        }
    }

    @Override
    public void stopScan(final BleDeviceScanCallback callback) {
        mBleScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBleScanner != null) {
            mBleScanner.stopScan(mScanCallback);
        }
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

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, final ScanResult result) {
            if (mCallback != null) {
                mCallback.onLeScan(result.getDevice(), result.getRssi());
            }
        }

        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
        }

        @Override
        public void onScanFailed(final int errorCode) {
            mCallback.onFail();
        }
    };
}
