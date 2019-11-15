/*
 HeartRateConnector
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import org.deviceconnect.android.deviceplugin.heartrate.ble.BleDeviceDetector;
import org.deviceconnect.android.deviceplugin.heartrate.ble.BleUtils;
import org.deviceconnect.android.deviceplugin.heartrate.data.HeartRateDevice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT16;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;

/**
 * This class manages a BLE device that have Heart Rate Service.
 * <p>
 * This class provides the following functions:
 * <li>Connect a GATT of Heart Rate Service</li>
 * <li>Disconnect a GATT of Heart Rate Service</li>
 * <li>Get heart rate</li>
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class HeartRateConnector {
    /** Logger. */
    private final Logger mLogger = Logger.getLogger("heartrate.dplugin");

    /**
     * Define the time to delay first execution.(ms)
     */
    private static final int CHK_FIRST_WAIT_PERIOD = 1000;

    /**
     * Define the period between successive executions.(ms)
     */
    private static final int CHK_WAIT_PERIOD = 20 * 1000;

    /**
     * application context.
     */
    private Context mContext;

    /**
     * Instance of HeartRateConnectEventListener.
     */
    private HeartRateConnectEventListener mListener;

    /**
     * Map of Device state.
     */
    private final Map<BluetoothGatt, DeviceState> mHRDevices = new ConcurrentHashMap<>();

    /**
     * List of address of device that registered.
     */
    private final List<String> mRegisterDevices = Collections.synchronizedList(
            new ArrayList<>());

    /**
     * Instance of ScheduledExecutorService.
     */
    private ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * ScheduledFuture of automatic connection timer.
     */
    private ScheduledFuture<?> mAutoConnectTimerFuture;

    /**
     * Instance of BleDeviceDetector.
     */
    private BleDeviceDetector mBleDeviceDetector;

    /**
     * Constructor.
     *
     * @param context application context
     * @param devices HeartRateDevice list
     */
    public HeartRateConnector(final Context context, final List<HeartRateDevice> devices) {
        mContext = context;

        for (HeartRateDevice device : devices) {
            mRegisterDevices.add(device.getAddress());
        }
    }

    /**
     * Sets a instance of BleDeviceDetector.
     * @param detector instance of BleDeviceDetector
     */
    public void setBleDeviceDetector(final BleDeviceDetector detector) {
        mBleDeviceDetector = detector;
    }

    /**
     * Sets a listener.
     *
     * @param listener listener
     */
    public void setListener(final HeartRateConnectEventListener listener) {
        mListener = listener;
    }

    /**
     * Connect to the bluetooth device.
     *
     * @param device bluetooth device
     */
    public void connectDevice(final BluetoothDevice device) {
        if (device == null) {
            throw new IllegalArgumentException("device is null");
        }
        if (containGattMap(device.getAddress())) {
            return;
        }
        try {
            device.connectGatt(mContext, false, mBluetoothGattCallback);
        } catch (Exception e) {
            // Exception occurred when the BLE state is invalid.
            mLogger.warning("Exception occurred.");
        }
    }

    /**
     * Disconnect to the bluetooth device.
     *
     * @param device bluetooth device
     */
    public void disconnectDevice(final BluetoothDevice device) {
        if (device == null) {
            throw new IllegalArgumentException("device is null");
        }
        String address = device.getAddress();
        synchronized (mHRDevices) {
            for (BluetoothGatt gatt : mHRDevices.keySet()) {
                if (gatt.getDevice().getAddress().equalsIgnoreCase(address)) {
                    gatt.disconnect();
                }
            }
        }
        mRegisterDevices.remove(device.getAddress());
    }

    /**
     * Gets a BluetoothDevice from device list.
     * @param list list
     * @param address address
     * @return Instance of BluetoothDevice, null if not found address
     */
    private BluetoothDevice getBluetoothDeviceFromDeviceList(
            final List<BluetoothDevice> list, final String address) {
        for (BluetoothDevice device : list) {
            if (address.equalsIgnoreCase(device.getAddress())) {
                return device;
            }
        }
        return null;
    }

    /**
     * Starts timer for automatic connection of BLE device.
     * <p>
     *     If timer has already started, this method do nothing.
     * </p>
     * <p>
     *     NOTE: The automatic connection was implemented on one's own,
     *           because the autoConnect flag of BluetoothDevice#connectGatt did not work as expected.
     * </p>
     * @throws IllegalStateException if {@link BleDeviceDetector} has not been set, this exception occur.
     */
    public synchronized void start() {
        if (mAutoConnectTimerFuture != null) {
            // timer has already started.
            return;
        }
        if (mBleDeviceDetector == null) {
            throw new IllegalStateException("BleDeviceDetector has not been set.");
        }
        mAutoConnectTimerFuture = mExecutor.scheduleAtFixedRate(() -> {
            mLogger.info("AutoConnect ");

            boolean foundOfflineDevice = false;
            for (String address : mRegisterDevices) {
                if (!containGattMap(address)) {
                    // Found the offline device.
                    foundOfflineDevice = true;
                }
            }
            if (foundOfflineDevice) {
                mLogger.info("Found an offline device.");

                mBleDeviceDetector.scanLeDeviceOnce((devices) -> {
                    synchronized (mRegisterDevices) {
                        for (String address : mRegisterDevices) {
                            if (!containGattMap(address)) {
                                BluetoothDevice device = getBluetoothDeviceFromDeviceList(devices, address);
                                if (device != null) {
                                    connectDevice(device);
                                }
                            }
                        }
                    }
                });
            }
        }, CHK_FIRST_WAIT_PERIOD, CHK_WAIT_PERIOD, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops timer for automatic connection of BLE device.
     */
    public synchronized void stop() {
        if (mAutoConnectTimerFuture != null) {
            mAutoConnectTimerFuture.cancel(true);
            mAutoConnectTimerFuture = null;
        }
        for (BluetoothGatt gatt : mHRDevices.keySet()) {
            gatt.close();
        }
        mHRDevices.clear();
    }

    /**
     * Tests whether this mHRDevices contains address.
     * @param address BLE device address
     * @return true if address is an element of mHRDevices, false otherwise
     */
    private boolean containGattMap(final String address) {
        synchronized (mHRDevices) {
            for (BluetoothGatt gatt : mHRDevices.keySet()) {
                if (gatt.getDevice().getAddress().equalsIgnoreCase(address)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tests whether BLE Device has Heart Rate Service.
     * @param gatt GATT Service
     * @return true BLE device has Heart Rate Service
     */
    private boolean hasHeartRateService(final BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(UUID.fromString(
                BleUtils.SERVICE_HEART_RATE_SERVICE));
        return service != null;
    }

    /**
     * Checks whether characteristic's uuid and checkUuid is same.
     * @param characteristic uuid
     * @param checkUuid uuid
     * @return true uuid is same, false otherwise
     */
    private boolean isCharacteristic(final BluetoothGattCharacteristic characteristic,
                                     final String checkUuid) {
        String uuid = characteristic.getUuid().toString();
        return checkUuid.equalsIgnoreCase(uuid);
    }

    /**
     * Checks whether characteristic is body sensor location.
     * @param characteristic uuid
     * @return true uuid is same, false otherwise
     */
    private boolean isBodySensorLocation(final BluetoothGattCharacteristic characteristic) {
        return isCharacteristic(characteristic, BleUtils.CHAR_BODY_SENSOR_LOCATION);
    }

    /**
     * Checks whether characteristic is Heart Rate Measurement
     * @param characteristic uuid
     * @return true uuid is same, false otherwise
     */
    private boolean isHeartRateMeasurement(final BluetoothGattCharacteristic characteristic) {
        return isCharacteristic(characteristic, BleUtils.CHAR_HEART_RATE_MEASUREMENT);
    }

    /**
     * Register a state of GATT Service and connects GATT Service.
     * @param gatt GATT
     */
    private void registerHeartRateDeviceState(final BluetoothGatt gatt) {
        mHRDevices.put(gatt, DeviceState.GET_LOCATION);
        if (mListener != null) {
            mListener.onConnected(gatt.getDevice());
        }
        if (!mRegisterDevices.contains(gatt.getDevice().getAddress())) {
            mRegisterDevices.add(gatt.getDevice().getAddress());
        }
    }

    /**
     * Get a body sensor location from GATT Service.
     *
     * @param gatt GATT Service
     * @return true if gatt has Generic Access Service, false if gatt has no service.
     */
    private boolean callGetBodySensorLocation(final BluetoothGatt gatt) {
        boolean result = false;
        BluetoothGattService service = gatt.getService(UUID.fromString(
                BleUtils.SERVICE_HEART_RATE_SERVICE));
        if (service != null) {
            BluetoothGattCharacteristic c = service.getCharacteristic(
                    UUID.fromString(BleUtils.CHAR_BODY_SENSOR_LOCATION));
            if (c != null) {
                result = gatt.readCharacteristic(c);
            }
        }
        return result;
    }

    /**
     * Register notification of HeartRateMeasurement Characteristic.
     *
     * @param gatt GATT Service
     * @return true if successful in notification of registration
     */
    private boolean callRegisterHeartRateMeasurement(final BluetoothGatt gatt) {
        boolean registered = false;
        BluetoothGattService service = gatt.getService(UUID.fromString(
                BleUtils.SERVICE_HEART_RATE_SERVICE));
        if (service != null) {
            BluetoothGattCharacteristic c = service.getCharacteristic(
                    UUID.fromString(BleUtils.CHAR_HEART_RATE_MEASUREMENT));
            if (c != null) {
                registered = gatt.setCharacteristicNotification(c, true);
                if (registered) {
                    for (BluetoothGattDescriptor descriptor : c.getDescriptors()) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }
                    mHRDevices.put(gatt, DeviceState.REGISTER_NOTIFY);
                }
            }
        }
        return registered;
    }

    /**
     * Shift to the next state on GATT Service.
     * @param gatt GATT Service
     */
    private void next(final BluetoothGatt gatt) {
        if (!mHRDevices.containsKey(gatt)) {
            registerHeartRateDeviceState(gatt);
        }

        DeviceState state = mHRDevices.get(gatt);
        switch (state) {
            case GET_LOCATION:
                if (!callGetBodySensorLocation(gatt)) {
                    mHRDevices.put(gatt, DeviceState.REGISTER_NOTIFY);
                    gatt.discoverServices();
                }
                break;
            case REGISTER_NOTIFY:
                if (!callRegisterHeartRateMeasurement(gatt)) {
                    mHRDevices.put(gatt, DeviceState.ERROR);
                }
                break;
            case CONNECTED:
                mLogger.fine("@@@@@@ GATT Service is connected.");
                break;
            default:
                mLogger.warning("Illegal state. state=" + state);
                break;
        }
    }

    /**
     * Notify heart rate to {@link HeartRateConnectEventListener}.
     * @param gatt GATT Service
     * @param characteristic BluetoothGattCharacteristic
     */
    private void notifyHeartRateMeasurement(final BluetoothGatt gatt,
            final BluetoothGattCharacteristic characteristic) {
        int heartRate = 0;
        int energyExpended = 0;
        double rrInterval = 0;
        int offset = 1;

        byte[] buf = characteristic.getValue();
        if (buf.length > 1) {
            // Heart Rate Value Format bit
            if ((buf[0] & 0x01) != 0) {
                Integer v = characteristic.getIntValue(FORMAT_UINT16, offset);
                if (v != null) {
                    heartRate = v;
                }
                offset += 2;
            } else {
                Integer v = characteristic.getIntValue(FORMAT_UINT8, offset);
                if (v != null) {
                    heartRate = v;
                }
                offset += 1;
            }

            // Sensor Contact Status bits
            if ((buf[0] & 0x06) != 0) {
                // MEMO: not implements yet
            }

            // Energy Expended Status bit
            if ((buf[0] & 0x08) != 0) {
                Integer v = characteristic.getIntValue(FORMAT_UINT16, offset);
                if (v != null) {
                    energyExpended = v;
                }
                offset += 2;
            }

            // RR-Interval bit
            if ((buf[0] & 0x10) != 0) {
                Integer v = characteristic.getIntValue(FORMAT_UINT16, offset);
                if (v != null) {
                    rrInterval = ((double) v / 1024.0) * 1000.0;
                }
            }
        }

        mLogger.warning("@@@@@@ HEART RATE[" + heartRate + ", "
                + energyExpended + ", " + rrInterval + "]");

        BluetoothDevice device = gatt.getDevice();
        if (mListener != null) {
            mListener.onReceivedData(device, heartRate, energyExpended, rrInterval);
        }
    }

    /**
     * This class is the implement of BluetoothGattCallback.
     */
    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt,
                                            final int status, final int newState) {
            mLogger.fine("@@@@@@ onConnectionStateChange: [" + gatt.getDevice() + "]: status: "
                    + status + " newState: " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                gatt.close();

                if (hasHeartRateService(gatt)) {
                    mHRDevices.remove(gatt);
                    if (mListener != null) {
                        mListener.onDisconnected(gatt.getDevice());
                    }
                } else {
                    if (mListener != null) {
                        mListener.onConnectFailed(gatt.getDevice());
                    }
                }
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            mLogger.fine("@@@@@@ onServicesDiscovered: [" + gatt.getDevice() + "]");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (!hasHeartRateService(gatt)) {
                    // ble device has no heart rate service.
                    gatt.close();
                    if (mListener != null) {
                        mListener.onConnectFailed(gatt.getDevice());
                    }
                } else {
                    next(gatt);
                }
            } else {
                // connect error
                gatt.close();
                if (mListener != null) {
                    mListener.onConnectFailed(gatt.getDevice());
                }
            }
        }

        @Override
        public void onCharacteristicRead(final BluetoothGatt gatt,
                                         final BluetoothGattCharacteristic characteristic, final int status) {
            mLogger.fine("@@@@@@ onCharacteristicRead: [" + gatt.getDevice() + "]");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (isBodySensorLocation(characteristic)) {
                    Integer location = characteristic.getIntValue(FORMAT_UINT8, 0);
                    if (mListener != null && location != null) {
                        mListener.onReadSensorLocation(gatt.getDevice(), location);
                    }
                }
            }
            mHRDevices.put(gatt, DeviceState.REGISTER_NOTIFY);
            gatt.discoverServices();
        }

        @Override
        public void onCharacteristicChanged(final BluetoothGatt gatt,
                                            final BluetoothGattCharacteristic characteristic) {
            mLogger.fine("@@@@@@ onCharacteristicChanged: [" + gatt.getDevice() + "]");
            if (isHeartRateMeasurement(characteristic)) {
                notifyHeartRateMeasurement(gatt, characteristic);
            }
        }
    };

    private enum DeviceState {
        GET_LOCATION,
        REGISTER_NOTIFY,
        CONNECTED,
        DISCONNECT,
        ERROR,
    }

    /**
     * This interface is used to implement {@link HeartRateConnector} callbacks.
     */
    public static interface HeartRateConnectEventListener {
        void onConnected(BluetoothDevice device);
        void onDisconnected(BluetoothDevice device);
        void onConnectFailed(BluetoothDevice device);
        void onReadSensorLocation(BluetoothDevice device, int location);
        void onReceivedData(BluetoothDevice device, int heartRate,
                            int energyExpended, double rrInterval);
    }
}
