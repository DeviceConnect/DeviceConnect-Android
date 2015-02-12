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
import org.deviceconnect.android.deviceplugin.heartrate.data.HeartRateDBHelper;
import org.deviceconnect.android.deviceplugin.heartrate.data.HeartRateData;
import org.deviceconnect.android.deviceplugin.heartrate.data.HeartRateDevice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static org.deviceconnect.android.deviceplugin.heartrate.HeartRateConnector.HeartRateConnectEventListener;
import static org.deviceconnect.android.deviceplugin.heartrate.ble.BleDeviceDetector.BleDeviceDiscoveryListener;

/**
 * @author NTT DOCOMO, INC.
 */
public class HeartRateManager {
    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("heartrate.dplugin");

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
    private List<HeartRateDevice> mRegisterDevices = Collections.synchronizedList(
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
        mConnector.setBleDeviceDetector(mDetector);

        mDBHelper = new HeartRateDBHelper(context);

        List<HeartRateDevice> list = mDBHelper.getHeartRateDevices();
        for (HeartRateDevice device : list) {
            if (device.isRegisterFlag()) {
                mRegisterDevices.add(device);
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
        mDetector.initialize();
        synchronized (mRegisterDevices) {
            for (HeartRateDevice device : mRegisterDevices) {
                connectBleDevice(device.getAddress());
            }
        }
        if (mDetector.isScanning()) {
            mDetector.startScan();
        }
        mConnector.start();
    }

    public void stop() {
        mConnectedDevices.clear();
        mConnector.stop();
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

    private HeartRateDevice registerHeartRateDevice(final BluetoothDevice device) {
        HeartRateDevice hr = new HeartRateDevice();
        hr.setName(device.getName());
        hr.setAddress(device.getAddress());
        hr.setRegisterFlag(true);
        hr.setConnectFlag(true);
        mDBHelper.addHeartRateDevice(hr);
        mRegisterDevices.add(hr);
        return hr;
    }

    private void unregisterHeartRateDevice(final String address) {
        HeartRateDevice hr = findRegisteredHeartRateDeviceByAddress(address);
        if (hr != null) {
            mDBHelper.removeHeartRateDevice(hr);
            mRegisterDevices.remove(hr);
        }
    }

    /**
     * Connect to GATT Server hosted by device.
     * @param address address for ble device
     */
    public void connectBleDevice(final String address) {
        BluetoothDevice blue = mDetector.getDevice(address);
        if (blue != null) {
            mConnector.connectDevice(blue);
        }
    }

    /**
     * Disconnect to GATT Server hosted by device.
     * @param address address for ble device
     */
    public void disconnectBleDevice(final String address) {
        BluetoothDevice blue = mDetector.getDevice(address);
        if (blue != null) {
            mConnector.disconnectDevice(blue);
        }
        unregisterHeartRateDevice(address);
    }

    public List<HeartRateDevice> getConnectedDevices() {
        return mConnectedDevices;
    }

    public List<HeartRateDevice> getRegisterDevices() {
        return mRegisterDevices;
    }

    public Set<BluetoothDevice> getBondedDevices() {
        return mDetector.getBondedDevices();
    }

    public HeartRateData getHeartRateData(String address) {
        HeartRateDevice device = findRegisteredHeartRateDeviceByAddress(address);
        return getHeartRateData(device);
    }

    public HeartRateData getHeartRateData(HeartRateDevice device) {
        return mHRData.get(device);
    }

    private HeartRateDevice findRegisteredHeartRateDeviceByAddress(String address) {
        for (HeartRateDevice d : mRegisterDevices) {
            if (d.getAddress().equalsIgnoreCase(address)) {
                return d;
            }
        }
        return null;
    }

    private HeartRateDevice findConnectedHeartRateDeviceByAddress(String address) {
        for (HeartRateDevice d : mConnectedDevices) {
            if (d.getAddress().equalsIgnoreCase(address)) {
                return d;
            }
        }
        return null;
    }

    /**
     * Tests whether this mRegisterDevices contains the specified object.
     * @param device device will be checked
     * @return true if object is an element of mRegisterDevices, false
     *         otherwise
     */
    private boolean containRegisteredHeartRateDevice(final BluetoothDevice device) {
        for (HeartRateDevice d : mRegisterDevices) {
            if (d.getAddress().equalsIgnoreCase(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Implementation of BleDeviceDiscoveryListener.
     */
    private BleDeviceDiscoveryListener mDiscoveryListener = new BleDeviceDiscoveryListener() {
        @Override
        public void onDiscovery(final List<BluetoothDevice> devices) {
            mLogger.fine("BleDeviceDiscoveryListener#onDiscovery: " + devices.size());
            if (mHRDiscoveryListener != null) {
                mHRDiscoveryListener.onDiscovery(devices);
            }
        }
    };

    /**
     * Implementation of HeartRateConnectEventListener.
     */
    private HeartRateConnectEventListener mHRConnectListener = new HeartRateConnectEventListener() {
        @Override
        public void onConnected(final BluetoothDevice device) {
            mLogger.fine("HeartRateConnectEventListener#onConnected: [" + device + "]");
            HeartRateDevice hr = findRegisteredHeartRateDeviceByAddress(device.getAddress());
            if (hr == null) {
                hr = registerHeartRateDevice(device);
            } else {
                hr.setConnectFlag(true);
            }
            mConnectedDevices.add(hr);
            if (mHRDiscoveryListener != null) {
                mHRDiscoveryListener.onConnected(device);
            }
        }

        @Override
        public void onDisconnected(final BluetoothDevice device) {
            mLogger.fine("HeartRateConnectEventListener#onDisconnected: [" + device + "]");
            HeartRateDevice hr = findConnectedHeartRateDeviceByAddress(device.getAddress());
            if (hr == null && !containRegisteredHeartRateDevice(device)) {
                if (mHRDiscoveryListener != null) {
                    mHRDiscoveryListener.onConnectFailed(device);
                }
            } else {
                mConnectedDevices.remove(hr);
            }
        }

        @Override
        public void onConnectFailed(final BluetoothDevice device) {
            mLogger.fine("HeartRateConnectEventListener#onConnectFailed: [" + device + "]");
            if (mHRDiscoveryListener != null) {
                mHRDiscoveryListener.onConnectFailed(device);
            }
        }

        @Override
        public void onReadSensorLocation(BluetoothDevice device, int location) {
            mLogger.fine("HeartRateConnectEventListener#onReadSensorLocation: ["
                    + device + "]: " + location);
            HeartRateDevice hr = findConnectedHeartRateDeviceByAddress(device.getAddress());
            if (hr != null) {
                hr.setSensorLocation(location);
                mDBHelper.updateHeartRateDevice(hr);
            }
        }

        @Override
        public void onReceivedData(final BluetoothDevice device, final int heartRate,
                final int energyExpended, final double rrInterval) {
            mLogger.fine("HeartRateConnectEventListener#onReceivedData: [" + device + "]");
            HeartRateDevice hr = findRegisteredHeartRateDeviceByAddress(device.getAddress());
            if (hr == null) {
                mLogger.warning("device not found. device:[" + device + "]");
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

    /**
     * This interface is used to implement {@link HeartRateManager} callbacks.
     */
    public static interface OnHeartRateDiscoveryListener {
        void onDiscovery(List<BluetoothDevice> devices);
        void onConnected(BluetoothDevice device);
        void onConnectFailed(BluetoothDevice device);
    }

    /**
     * This interface is used to implement {@link HeartRateManager} callbacks.
     */
    public static interface OnHeartRateEventListener {
        void onReceivedData(HeartRateDevice device, HeartRateData data);
    }
}
