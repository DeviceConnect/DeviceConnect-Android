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
import android.util.Log;

import org.deviceconnect.android.deviceplugin.heartrate.ble.BleUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT16;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;

/**
 * @author NTT DOCOMO, INC.
 */
public class HeartRateConnector {
    /**
     * application context.
     */
    private Context mContext;

    /**
     * Instance of HeartRateConnectEventListener.
     */
    private HeartRateConnectEventListener mListener;

    private Map<BluetoothGatt, DeviceState> mHRDevices = new HashMap<>();

    private List<String> mCheckDevices = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param context application context
     */
    public HeartRateConnector(Context context) {
        mContext = context;
    }

    /**
     * Sets a listener.
     *
     * @param listener listener
     */
    public void setListener(HeartRateConnectEventListener listener) {
        mListener = listener;
    }

    public void checkHeartRateDevice(final BluetoothDevice device, final HeartRateDeviceCheckListener listener) {
        if (device == null) {
            throw new IllegalArgumentException("device is null");
        }

//        if (checkedDevicePreviously(device)) {
//            if (listener != null) {
//                listener.onChecked(device, false);
//            }
//            return;
//        }

        device.connectGatt(mContext, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    if (listener != null) {
                        listener.onChecked(device, false);
                    }
                    gatt.disconnect();
                    gatt.close();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (hasHeartRateService(gatt)) {
                        if (listener != null) {
                            listener.onChecked(device, true);
                        }
                    } else {
                        if (listener != null) {
                            listener.onChecked(device, false);
                        }
                        mCheckDevices.add(device.getAddress());
                    }
                } else {
                    if (listener != null) {
                        listener.onChecked(device, false);
                    }
                }
                gatt.disconnect();
                gatt.close();
            }
        });
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
        device.connectGatt(mContext, false, mBluetoothGattCallback);
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
    }

    private boolean checkedDevicePreviously(final BluetoothDevice device) {
        String address = device.getAddress();
        return mCheckDevices.contains(address);
    }

    /**
     * Checks Ble Device has Heart Rate Service.
     *
     * @param gatt GATT Service
     * @return true ble device has Heart Rate Service
     */
    private boolean hasHeartRateService(BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(UUID.fromString(
                BleUtils.SERVICE_HEART_RATE_SERVICE));
        return service != null;
    }

    private boolean isCharacteristic(final BluetoothGattCharacteristic characteristic,
                                     final String checkUuid) {
        String uuid = characteristic.getUuid().toString();
        return checkUuid.equalsIgnoreCase(uuid);
    }

    private boolean isBodySensorLocation(final BluetoothGattCharacteristic characteristic) {
        return isCharacteristic(characteristic, BleUtils.CHAR_BODY_SENSOR_LOCATION);
    }

    private boolean isHeartRateMeasurement(final BluetoothGattCharacteristic characteristic) {
        return isCharacteristic(characteristic, BleUtils.CHAR_HEART_RATE_MEASUREMENT);
    }

    private void newHeartRateDevice(final BluetoothGatt gatt) {
        if (mListener != null) {
            mListener.onConnected(gatt.getDevice());
        }
        mHRDevices.put(gatt, DeviceState.GET_LOCATION);
    }

    /**
     * Get a body sensor location from GATT Service.
     *
     * @param gatt GATT Service
     * @return true if gatt has Generic Access Service, false if gatt has no service.
     */
    private boolean callGetBodySensorLocation(BluetoothGatt gatt) {
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
     * @return
     */
    private boolean callRegisterHeartRateMeasurement(BluetoothGatt gatt) {
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

    private boolean next(final BluetoothGatt gatt) {
        if (!mHRDevices.containsKey(gatt)) {
            newHeartRateDevice(gatt);
        }
        Log.i("ABC", "@@@@@@ next: ");

        DeviceState state = mHRDevices.get(gatt);
        switch (state) {
            case GET_LOCATION:
                Log.i("ABC", "@@@@@@ GET_LOCATION: ");
                if (!callGetBodySensorLocation(gatt)) {
                    mHRDevices.put(gatt, DeviceState.REGISTER_NOTIFY);
                    gatt.discoverServices();
                }
                break;
            case REGISTER_NOTIFY:
                Log.i("ABC", "@@@@@@ REGISTER_NOTIFY: ");
                if (!callRegisterHeartRateMeasurement(gatt)) {
                    mHRDevices.put(gatt, DeviceState.ERROR);
                }
                break;
            default:
                break;
        }

        return false;
    }


    private void notifyHeartRateMeasurement(final BluetoothGatt gatt,
            final BluetoothGattCharacteristic characteristic) {
        int heartRate = 0;
        int energyExpended = 0;
        double rrInterval = 0;
        int offset = 1;

        byte[] buf = characteristic.getValue();
        if (buf.length > 1) {
            // Heart Rate Value Format bit
            if ((buf[0] & 0x80) != 0) {
                heartRate = characteristic.getIntValue(FORMAT_UINT16, offset);
                offset += 2;
            } else {
                heartRate = characteristic.getIntValue(FORMAT_UINT8, offset);
                offset += 1;
            }

            // Sensor Contact Status bits
            if ((buf[0] & 0x60) != 0) {
            }

            // Energy Expended Status bit
            if ((buf[0] & 0x10) != 0) {
                energyExpended = characteristic.getIntValue(FORMAT_UINT16, offset);
                offset += 2;
            }

            // RR-Interval bit
            if ((buf[0] & 0x08) != 0) {
                int value = characteristic.getIntValue(FORMAT_UINT16, offset);
                rrInterval = ((double) value / 1024.0) * 1000.0;
            }
        }

        if (BuildConfig.DEBUG) {
            Log.i("ABC", "HEART RATE: " + heartRate);
            Log.i("ABC", "EnergyExpended: " + energyExpended);
            Log.i("ABC", "RR-Interval: " + rrInterval);
        }

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
            Log.i("ABC", "@@@@@@ onConnectionStateChange: " + status + " -> " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.w("ABC", "discoverServices");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                gatt.close();
                if (mListener != null) {
                    mListener.onDisconnected(gatt.getDevice());
                }
                mHRDevices.remove(gatt);
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            Log.w("ABC", "onServicesDiscovered");
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
                    mListener.onDisconnected(gatt.getDevice());
                }
            }
        }

        @Override
        public void onCharacteristicRead(final BluetoothGatt gatt,
                                         final BluetoothGattCharacteristic characteristic, final int status) {
            Log.i("ABC", "@@@@@@ onCharacteristicRead: ");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (isBodySensorLocation(characteristic)) {
                }
            }
            mHRDevices.put(gatt, DeviceState.REGISTER_NOTIFY);
            gatt.discoverServices();
        }

        @Override
        public void onCharacteristicChanged(final BluetoothGatt gatt,
                                            final BluetoothGattCharacteristic characteristic) {
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

    public static interface HeartRateDeviceCheckListener {
        void onChecked(BluetoothDevice device, boolean checked);
    }

    /**
     * This interface is used to implement {@link HeartRateConnector} callbacks.
     */
    public static interface HeartRateConnectEventListener {
        void onConnected(BluetoothDevice device);

        void onDisconnected(BluetoothDevice device);

        void onConnectFailed(BluetoothDevice device);

        void onReceivedData(BluetoothDevice device, int heartRate,
                            int energyExpended, double rrInterval);
    }
}
