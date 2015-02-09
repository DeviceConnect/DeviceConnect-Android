/*
 HeartRateManager
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import org.deviceconnect.android.deviceplugin.heartrate.ble.BleDeviceDetector;
import org.deviceconnect.android.deviceplugin.heartrate.data.HeartRateDevice;

import java.util.ArrayList;
import java.util.List;

import static org.deviceconnect.android.deviceplugin.heartrate.HeartRateConnector.*;
import static org.deviceconnect.android.deviceplugin.heartrate.HeartRateConnector.HeartRateConnectEventListener;
import static org.deviceconnect.android.deviceplugin.heartrate.ble.BleDeviceDetector.BleDeviceDiscoveryListener;

/**
 * @author NTT DOCOMO, INC.
 */
public class HeartRateManager {
    /**
     * application context.
     */
    private Context mContext;

    /**
     * Instance of BleDeviceDetector.
     */
    private BleDeviceDetector mDetector;

    /**
     * Instance of HeartRateConnector.
     */
    private HeartRateConnector mConnector;

    private OnHeartRateDiscoveryListener mBleListener;
    private OnHeartRateEventListener mHRListener;

    private List<BluetoothDevice> mConnectedDevices;
    private List<BluetoothDevice> mDiscoveryDevices = new ArrayList<>();

    /**
     * Constructor.
     * @param context application context
     */
    public HeartRateManager(Context context) {
        mContext = context;

        mDetector = new BleDeviceDetector(context);
        mDetector.setListener(mDiscoveryListener);

        mConnector = new HeartRateConnector(context);
        mConnector.setListener(mHREvtListener);
    }

    public void setOnBleDeviceEventListener(OnHeartRateDiscoveryListener listener) {
        mBleListener = listener;
    }

    /**
     * Starts ble scan.
     */
    public void startScanBle() {
        mDetector.startScan();
    }

    /**
     * Stops ble scan.
     */
    public void stopScanBle() {
        mDetector.stopScan();
    }

    /**
     * Connect to GATT Server hosted by device.
     * @param device device that connect to GATT
     */
    public void connectBleDevice(final HeartRateDevice device) {
    }

    /**
     * Disconnect to GATT Server hosted by device.
     * @param device device that disconnect to GATT
     */
    public void disconnectBleDevice(final HeartRateDevice device) {
    }

    /**
     * Checks whether device has Heart Rate Service.
     * @param device device will be checked
     */
    private void checkDevice(final BluetoothDevice device) {
        mConnector.checkHeartRateDevice(device, new HeartRateDeviceCheckListener() {
            @Override
            public void onChecked(final BluetoothDevice device, final boolean checked) {
                if (checked) {
                    mDiscoveryDevices.add(device);
                }
            }
        });
    }

    private BleDeviceDiscoveryListener mDiscoveryListener = new BleDeviceDiscoveryListener() {
        @Override
        public void onDiscovery(final List<BluetoothDevice> devices) {
            for (BluetoothDevice device : devices) {
                if (!mDiscoveryDevices.contains(device)) {
                    checkDevice(device);
                }
            }
        }
    };

    private HeartRateConnectEventListener mHREvtListener = new HeartRateConnectEventListener() {
        @Override
        public void onConnected(final BluetoothDevice device) {
        }

        @Override
        public void onDisconnected(final BluetoothDevice device) {
        }

        @Override
        public void onConnectFailed(final BluetoothDevice device) {
        }

        @Override
        public void onReceivedData(final BluetoothDevice device, final int heartRate,
                final int energyExpended, final int rrInterval) {
        }
    };

    public static interface OnHeartRateDiscoveryListener {
        void onDiscovery(List<HeartRateDevice> devices);
    }

    public static interface OnHeartRateEventListener {
    }
}
