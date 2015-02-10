/*
 HeartRateManager
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.heartrate.ble.BleDeviceDetector;
import org.deviceconnect.android.deviceplugin.heartrate.data.HeartRateDBHelper;
import org.deviceconnect.android.deviceplugin.heartrate.data.HeartRateData;
import org.deviceconnect.android.deviceplugin.heartrate.data.HeartRateDevice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.deviceconnect.android.deviceplugin.heartrate.HeartRateConnector.HeartRateConnectEventListener;
import static org.deviceconnect.android.deviceplugin.heartrate.HeartRateConnector.HeartRateDeviceCheckListener;
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

    private HeartRateDBHelper mDBHelper;

    private OnHeartRateDiscoveryListener mHRDiscoveryListener;
    private OnHeartRateEventListener mHREvtListener;

    // TODO: consider synchronized
    private List<HeartRateDevice> mConnectedDevices = Collections.synchronizedList(
            new ArrayList<HeartRateDevice>());
    private List<HeartRateDevice> mDiscoveryDevices = Collections.synchronizedList(
            new ArrayList<HeartRateDevice>());
    private Map<HeartRateDevice, HeartRateData> mHRData = new ConcurrentHashMap<>();

    /**
     * Constructor.
     * @param context application context
     */
    public HeartRateManager(Context context) {
        mContext = context;

        mDetector = new BleDeviceDetector(context);
        mDetector.setListener(mDiscoveryListener);

        mConnector = new HeartRateConnector(context);
        mConnector.setListener(mHRConnectListener);

        mDBHelper = new HeartRateDBHelper(context);

        List<HeartRateDevice> list = mDBHelper.getHeartRateDevices();
        for (HeartRateDevice device : list) {
            if (device.isRegisterFlag()) {
                mDiscoveryDevices.add(device);
            }
        }

        if (mDetector.isEnabled()) {
            start();
        }
    }

    public void setOnHeartRateDiscoveryListener(OnHeartRateDiscoveryListener listener) {
        mHRDiscoveryListener = listener;
    }

    public void start() {
        synchronized (mDiscoveryDevices) {
            for (HeartRateDevice device : mDiscoveryDevices) {
                if (device.isRegisterFlag()) {
                    connectBleDevice(device);
                }
            }
        }
    }

    public void stop() {
        synchronized (mConnectedDevices) {
            for (HeartRateDevice device : mConnectedDevices) {
                disconnectBleDevice(device);
            }
        }
        mConnectedDevices.clear();
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

    public void registerHeartRateDevice(final HeartRateDevice device) {
        connectBleDevice(device);
        device.setRegisterFlag(true);
        if (device.getId() == -1) {
            mDBHelper.addHeartRateDevice(device);
        } else {
            mDBHelper.updateHeartRateDevice(device);
        }
    }

    public void unregisterHeartRateDevice(final HeartRateDevice device) {
        disconnectBleDevice(device);
        device.setRegisterFlag(false);
        mDBHelper.updateHeartRateDevice(device);
    }


    /**
     * Connect to GATT Server hosted by device.
     * @param device device that connect to GATT
     */
    private void connectBleDevice(final HeartRateDevice device) {
        BluetoothDevice blue = mDetector.getDevice(device.getAddress());
        if (blue != null) {
            mConnector.connectDevice(blue);
        }
        Log.e("ABC", "connectBleDevice: " + device);
    }

    /**
     * Disconnect to GATT Server hosted by device.
     * @param device device that disconnect to GATT
     */
    private void disconnectBleDevice(final HeartRateDevice device) {
        BluetoothDevice blue = mDetector.getDevice(device.getAddress());
        if (blue != null) {
            mConnector.disconnectDevice(blue);
        }
        device.setConnectFlag(false);
    }

    public List<HeartRateDevice> getConnectedDevices() {
        return mConnectedDevices;
    }

    public List<HeartRateDevice> getDiscoveryDevices() {
        return mDiscoveryDevices;
    }

    public HeartRateData getHeartRateData(String address) {
        HeartRateDevice device = findHeartRateDeviceByAddress(address);
        return getHeartRateData(device);
    }

    public HeartRateData getHeartRateData(HeartRateDevice device) {
        return mHRData.get(device);
    }

    private HeartRateDevice findHeartRateDeviceByAddress(String address) {
        for (HeartRateDevice d : mDiscoveryDevices) {
            if (d.getAddress().equalsIgnoreCase(address)) {
                return d;
            }
        }
        return null;
    }

    /**
     * Checks whether device has Heart Rate Service.
     * @param device device will be checked
     */
    private void checkDevice(final BluetoothDevice device) {
        Log.e("ABC", "checkDevice: " + device);
        mConnector.checkHeartRateDevice(device, new HeartRateDeviceCheckListener() {
            @Override
            public void onChecked(final BluetoothDevice device, final boolean checked) {
                if (checked) {
                    Log.e("ABC", "onChecked: " + device + " " + checked);
                    HeartRateDevice hr = new HeartRateDevice();
                    hr.setAddress(device.getAddress());
                    hr.setName(device.getName());
                    hr.setRegisterFlag(false);

                    if (!mDiscoveryDevices.contains(hr)) {
                        mDiscoveryDevices.add(hr);
                        mHRDiscoveryListener.onDiscovery(mDiscoveryDevices);
                    }
                }
            }
        });
    }

    /**
     * Tests whether this mDiscoveryDevices contains the specified object.
     * @param device device will be checked
     * @return true if object is an element of mDiscoveryDevices, false
     *         otherwise
     */
    private boolean containDevices(final BluetoothDevice device) {
        for (HeartRateDevice d : mDiscoveryDevices) {
            if (d.getAddress().equalsIgnoreCase(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    private BleDeviceDiscoveryListener mDiscoveryListener = new BleDeviceDiscoveryListener() {
        @Override
        public void onDiscovery(final List<BluetoothDevice> devices) {
            Log.e("ABC", "onDiscovery: " + devices.size());
            for (BluetoothDevice device : devices) {
                if (!containDevices(device)) {
                    checkDevice(device);
                }
            }
        }
    };

    private HeartRateConnectEventListener mHRConnectListener = new HeartRateConnectEventListener() {
        @Override
        public void onConnected(final BluetoothDevice device) {
            HeartRateDevice hr = findHeartRateDeviceByAddress(device.getAddress());
            hr.setConnectFlag(true);
            mConnectedDevices.add(hr);
            Log.e("ABC", "onConnected: " + device);
        }

        @Override
        public void onDisconnected(final BluetoothDevice device) {
            HeartRateDevice hr = findHeartRateDeviceByAddress(device.getAddress());
            hr.setConnectFlag(false);
            mConnectedDevices.remove(hr);
        }

        @Override
        public void onConnectFailed(final BluetoothDevice device) {
        }

        @Override
        public void onReceivedData(final BluetoothDevice device, final int heartRate,
                final int energyExpended, final double rrInterval) {
            Log.e("ABC", device + ": " + heartRate);
            HeartRateDevice hr = findHeartRateDeviceByAddress(device.getAddress());
            if (hr != null) {
                return;
            }

            HeartRateData data = new HeartRateData();
            data.setHeartRate(heartRate);
            data.setEnergyExpended(energyExpended);
            data.setRRInterval(rrInterval);

            mHRData.put(hr, data);

            if (mHREvtListener != null) {
                mHREvtListener.onReceivedData(hr, data);
            }
        }
    };

    public static interface OnHeartRateDiscoveryListener {
        void onDiscovery(List<HeartRateDevice> devices);
    }

    public static interface OnHeartRateEventListener {
        void onReceivedData(HeartRateDevice device, HeartRateData data);
    }
}
