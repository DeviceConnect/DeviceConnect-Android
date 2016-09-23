/*
 HeartRateManager
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

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
 * This class manages a BLE device and GATT Service.
 * <p>
 * This class provides the following functions:
 * <li>Scan a BLE device</li>
 * <li>Connect a GATT of Heart Rate Service</li>
 * <li>Get heart rate</li>
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
public class HeartRateManager {
    /**
     * Logger.
     */
    private final Logger mLogger = Logger.getLogger("heartrate.dplugin");

    /**
     * Application context.
     */
    private Context mContext;

    /**
     * Instance of {@link BleDeviceDetector}.
     */
    private BleDeviceDetector mDetector;

    /**
     * Instance of {@link HeartRateConnector}.
     */
    private HeartRateConnector mConnector;

    /**
     * Instance of {@link HeartRateDBHelper}.
     */
    private HeartRateDBHelper mDBHelper;

    private List<OnHeartRateDiscoveryListener> mHRDiscoveryListener;
    private OnHeartRateEventListener mHREvtListener;

    // TODO: consider synchronized
    private final List<HeartRateDevice> mConnectedDevices = Collections.synchronizedList(
            new ArrayList<HeartRateDevice>());
    private final List<HeartRateDevice> mRegisterDevices = Collections.synchronizedList(
            new ArrayList<HeartRateDevice>());
    private final Map<HeartRateDevice, HeartRateData> mHRData = new ConcurrentHashMap<>();

    private Handler mHandler = new Handler();

    /**
     * Constructor.
     *
     * @param context application context
     */
    public HeartRateManager(final Context context) {
        mContext = context;
        mHRDiscoveryListener = new ArrayList<>();
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

    /**
     * Sets the OnHeartRateDiscoveryListener.
     *
     * @param listener The listener to be told when found device or connected device
     */
    public void addOnHeartRateDiscoveryListener(OnHeartRateDiscoveryListener listener) {
        mHRDiscoveryListener.add(listener);
    }

    /**
     * Remove the OnHeartRateDiscoveryListener.
     * @param listener The listener to be told when found device or connected device
     */
    public void removeOnHeartRateDiscoveryListener(OnHeartRateDiscoveryListener listener) {
        mHRDiscoveryListener.remove(listener);
    }

    /**
     * Sets the OnHeartRateEventListener.
     *
     * @param listener The listener to be told when get a data of heart rate
     */
    public void setOnHeartRateEventListener(OnHeartRateEventListener listener) {
        mHREvtListener = listener;
    }

    /**
     * Starts the HeartRateManager.
     */
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

    /**
     * Stops the HeartRateManager.
     */
    public void stop() {
        mConnectedDevices.clear();
        mConnector.stop();
    }

    public boolean isEnabledBle() {
        if (mDetector == null) {
            return false;
        }
        return mDetector.isEnabled();
    }

    /**
     * Starts BLE scan.
     */
    public void startScanBle() {
        mDetector.startScan();
    }

    /**
     * Stops BLE scan.
     */
    public void stopScanBle() {
        mDetector.stopScan();
    }

    /**
     * Register the {@link HeartRateDevice} to database from BluetoothDevice.
     *
     * @param device Instance of BluetoothDevice
     * @return {@link HeartRateDevice}
     */
    private HeartRateDevice registerHeartRateDevice(final BluetoothDevice device) {
        HeartRateDevice hr = new HeartRateDevice();
        hr.setName(device.getName());
        hr.setAddress(device.getAddress());
        hr.setRegisterFlag(true);
        mDBHelper.addHeartRateDevice(hr);
        mRegisterDevices.add(hr);
        return hr;
    }

    /**
     * Unregister the {@link HeartRateDevice} to database from BluetoothDevice.
     *
     * @param address address of device
     */
    private void unregisterHeartRateDevice(final String address) {
        HeartRateDevice hr = findRegisteredHeartRateDeviceByAddress(address);
        if (hr != null) {
            mDBHelper.removeHeartRateDevice(hr);
            mRegisterDevices.remove(hr);
        }
    }

    /**
     * Connect to GATT Server by address.
     *
     * @param address address for ble device
     */
    public void connectBleDevice(final String address) {
        BluetoothDevice blue = mDetector.getDevice(address);
        if (blue != null) {
            mConnector.connectDevice(blue);
        }
    }

    /**
     * Disconnect to GATT Server by address.
     *
     * @param address address for ble device
     */
    public void disconnectBleDevice(final String address) {
        BluetoothDevice blue = mDetector.getDevice(address);
        if (blue != null) {
            mConnector.disconnectDevice(blue);
        }
        unregisterHeartRateDevice(address);
    }

    /**
     * Gets the list of BLE device that connected.
     *
     * @return list of BLE device
     */
    public List<HeartRateDevice> getConnectedDevices() {
        return mConnectedDevices;
    }

    /**
     * Gets the list of BLE device that was registered to automatic connection.
     *
     * @return list of BLE device
     */
    public List<HeartRateDevice> getRegisterDevices() {
        return mRegisterDevices;
    }

    /**
     * Gets the set of BluetoothDevice that are bonded (paired) to the local adapter.
     *
     * @return set of BluetoothDevice, or null on error
     */
    public Set<BluetoothDevice> getBondedDevices() {
        return mDetector.getBondedDevices();
    }

    /**
     * Gets the {@link HeartRateData} from address.
     *
     * @param address address of ble device
     * @return {@link HeartRateData}, or null on error
     */
    public HeartRateData getHeartRateData(final String address) {
        HeartRateDevice device = findRegisteredHeartRateDeviceByAddress(address);
        if (device == null) {
            return null;
        }
        return getHeartRateData(device);
    }

    /**
     * Gets the {@link HeartRateData} from {@link HeartRateDevice}.
     *
     * @param device Instance of {@link HeartRateDevice}
     * @return {@link HeartRateData}, or null on error
     */
    public HeartRateData getHeartRateData(final HeartRateDevice device) {
        return mHRData.get(device);
    }

    /**
     * Find the {@link HeartRateDevice} from address.
     *
     * @param address address of ble device
     * @return {@link HeartRateDevice}, or null
     */
    private HeartRateDevice findRegisteredHeartRateDeviceByAddress(final String address) {
        synchronized (mRegisterDevices) {
            for (HeartRateDevice d : mRegisterDevices) {
                if (d.getAddress().equalsIgnoreCase(address)) {
                    return d;
                }
            }
        }
        return null;
    }

    /**
     * Find the {@link HeartRateDevice} from address.
     *
     * @param address address of ble device
     * @return {@link HeartRateDevice}, or null
     */
    private HeartRateDevice findConnectedHeartRateDeviceByAddress(final String address) {
        synchronized (mConnectedDevices) {
            for (HeartRateDevice d : mConnectedDevices) {
                if (d.getAddress().equalsIgnoreCase(address)) {
                    return d;
                }
            }
        }
        return null;
    }

    /**
     * Tests whether this mRegisterDevices contains the BluetoothDevice.
     *
     * @param device device will be checked
     * @return true if object is an element of mRegisterDevices, false
     * otherwise
     */
    private boolean containRegisteredHeartRateDevice(final BluetoothDevice device) {
        synchronized (mRegisterDevices) {
            for (HeartRateDevice d : mRegisterDevices) {
                if (d.getAddress().equalsIgnoreCase(device.getAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tests whether this mConnectedDevices contains the address.
     * @param address address will be checked
     * @return true if address is an element of mConnectedDevices, false otherwise
     */
    public boolean containConnectedHeartRateDevice(final String address) {
        synchronized (mConnectedDevices) {
            for (HeartRateDevice d : mConnectedDevices) {
                if (d.getAddress().equalsIgnoreCase(address)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Implementation of BleDeviceDiscoveryListener.
     */
    private final BleDeviceDiscoveryListener mDiscoveryListener = new BleDeviceDiscoveryListener() {
        @Override
        public void onDiscovery(final List<BluetoothDevice> devices) {
            mLogger.fine("BleDeviceDiscoveryListener#onDiscovery: " + devices.size());
            if (mHRDiscoveryListener != null) {
                for (OnHeartRateDiscoveryListener l : mHRDiscoveryListener) {
                    l.onDiscovery(devices);
                }
            }
        }
    };

    /**
     * Implementation of HeartRateConnectEventListener.
     */
    private final HeartRateConnectEventListener mHRConnectListener = new HeartRateConnectEventListener() {
        @Override
        public void onConnected(final BluetoothDevice device) {
            mLogger.fine("HeartRateConnectEventListener#onConnected: [" + device + "]");
            HeartRateDevice hr = findRegisteredHeartRateDeviceByAddress(device.getAddress());
            if (hr == null) {
                hr = registerHeartRateDevice(device);
            }
            if (!mConnectedDevices.contains(hr)) {
                mConnectedDevices.add(hr);
            }
            if (mHRDiscoveryListener != null) {
                for (OnHeartRateDiscoveryListener l : mHRDiscoveryListener) {
                    l.onConnected(device);
                }
            }

            // DEBUG
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        String name = device.getName();
                        if (name == null) {
                            name = device.getAddress();
                        }
                        Toast.makeText(mContext, "Connect to " + name,
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        mLogger.warning("Exception occurred.");
                    }
                }
            });
        }

        @Override
        public void onDisconnected(final BluetoothDevice device) {
            mLogger.fine("HeartRateConnectEventListener#onDisconnected: [" + device + "]");
            HeartRateDevice hr = findConnectedHeartRateDeviceByAddress(device.getAddress());
            if (hr != null) {
                mConnectedDevices.remove(hr);
            }

            if (hr == null) {
                if (mHRDiscoveryListener != null) {
                    for (OnHeartRateDiscoveryListener l : mHRDiscoveryListener) {
                        l.onConnectFailed(device);
                    }
                }
            } else {
                if (mHRDiscoveryListener != null) {
                    for (OnHeartRateDiscoveryListener l : mHRDiscoveryListener) {
                        l.onDisconnected(device);
                    }
                }

                // DEBUG
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String name = device.getName();
                            if (name == null) {
                                name = device.getAddress();
                            }
                            Toast.makeText(mContext, "Disconnect to " + name,
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            mLogger.warning("Exception occurred.");
                        }
                    }
                });
            }
        }

        @Override
        public void onConnectFailed(final BluetoothDevice device) {
            mLogger.fine("HeartRateConnectEventListener#onConnectFailed: [" + device + "]");
            if (mHRDiscoveryListener != null) {
                for (OnHeartRateDiscoveryListener l : mHRDiscoveryListener) {
                    l.onConnectFailed(device);
                }
            }
        }

        @Override
        public void onReadSensorLocation(final BluetoothDevice device, final int location) {
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
    public interface OnHeartRateDiscoveryListener {
        void onDiscovery(List<BluetoothDevice> devices);

        void onConnected(BluetoothDevice device);

        void onConnectFailed(BluetoothDevice device);
        void onDisconnected(BluetoothDevice device);
    }

    /**
     * This interface is used to implement {@link HeartRateManager} callbacks.
     */
    public interface OnHeartRateEventListener {
        void onReceivedData(HeartRateDevice device, HeartRateData data);
    }
}
