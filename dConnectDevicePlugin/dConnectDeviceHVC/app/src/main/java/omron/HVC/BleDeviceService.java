/*
 * Copyright (C) 2014 OMRON Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package omron.HVC;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.UUID;

import org.deviceconnect.android.deviceplugin.hvc.BuildConfig;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BleDeviceService {
    
    /**
     * DebugLog.
     */
    static final boolean DEBUG_LOG = BuildConfig.DEBUG;
    
    /** status. */
    public static final int STATE_DISCONNECTED = 0;
    /** status. */
    public static final int STATE_CONNECTING = 1;
    /** status. */
    public static final int STATE_CONNECTED = 2;

    /** action. */
    public static final String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    /** action. */
    public static final String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    /** action. */
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    /** action. */
    public static final String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
    /** data. */
	public static final String NAME_DATA = ":NAME_DATA";
    /** data. */
    public static final String EXTRA_DATA = ":EXTRA_DATA";
    /** value. */
    public static final String DEVICE_DOES_NOT_SUPPORT_UART = "DEVICE_DOES_NOT_SUPPORT_UART";

    /** CCCD. */
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /** UUID2. */
    public static final UUID RX_SERVICE_UUID2 = UUID.fromString("35100001-d13a-4f39-8ab3-bf64d4fbb4b4");
    /** UUID2. */
    public static final UUID RX_CHAR_UUID2 = UUID.fromString("35100002-d13a-4f39-8ab3-bf64d4fbb4b4");
    /** UUID2. */
    public static final UUID TX_CHAR_UUID2 = UUID.fromString("35100003-d13a-4f39-8ab3-bf64d4fbb4b4");

    /** UUID. */
    public static final UUID NAME_CHAR_UUID = UUID.fromString("35100004-d13a-4f39-8ab3-bf64d4fbb4b4");

    /** BleCallback. */
    private BleCallback mCallBack = null;

    /** BluetoothGatt. */
    private BluetoothGatt mBluetoothGatt;
    /** BluetoothDeviceAddress. */
    private String mBluetoothDeviceAddress;
    /** ConnectionState. */
    private int mConnectionState = STATE_DISCONNECTED;

    /** TAG. */
    private static final String TAG = "BleDeviceService";

    /**
     * Implements callback methods for GATT events that the app cares about.  For example,
     * connection change and services discovered.
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            String intentAction;

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                setmConnectionState(STATE_CONNECTED);
                broadcastUpdate(intentAction);
                if (DEBUG_LOG) {
                    Log.i(TAG, "Connected to GATT server.");
                }
                // Attempts to discover services after successful connection.
                boolean result = mBluetoothGatt.discoverServices();
                if (DEBUG_LOG) {
                    Log.i(TAG, "Attempting to start service discovery:"
                        + result);
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                setmConnectionState(STATE_DISCONNECTED);
                if (DEBUG_LOG) {
                    Log.i(TAG, "Disconnected from GATT server.");
                }
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (DEBUG_LOG) {
                    Log.w(TAG, "mBluetoothGatt = " + mBluetoothGatt);
                }
                BluetoothGattCharacteristic txChar;
                BluetoothGattService rxService = mBluetoothGatt.getService(RX_SERVICE_UUID2);
                if (rxService == null) {
                    showMessage("mBluetoothGatt null" + mBluetoothGatt);
                    showMessage("Rx service not found!");
                    broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
                    return;
                }
                txChar = rxService.getCharacteristic(TX_CHAR_UUID2);
                if (txChar == null) {
                    showMessage("Rx charateristic not found!");
                    broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
                    return;
                }
                if (DEBUG_LOG) {
                    Log.w(TAG, "RxChar = " + TX_CHAR_UUID2.toString());
                }
                mBluetoothGatt.setCharacteristicNotification(txChar, true);

                BluetoothGattDescriptor descriptor = txChar.getDescriptor(CCCD);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);

                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                if (DEBUG_LOG) {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                }
            }
        }

        @Override
        public void onCharacteristicRead(final BluetoothGatt gatt,
                final BluetoothGattCharacteristic characteristic,
                final int status) {
            if (DEBUG_LOG) {
                Log.d(TAG, String.format("onCharacteristicRead: %s", characteristic.getUuid()));
            }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(final BluetoothGatt gatt,
                final BluetoothGattCharacteristic characteristic) {
            if (DEBUG_LOG) {
                Log.d(TAG, String.format("onCharacteristicChanged: %s", characteristic.getUuid()));
            }
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    /**
     * broadcastUpdate.
     * @param action action
     */
    private void broadcastUpdate(final String action) {
        mCallBack.callbackMethod(action);
    }

    /**
     * broadcastUpdate.
     * @param action action
     * @param characteristic characteristic
     */
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/
        //         Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (TX_CHAR_UUID2.equals(characteristic.getUuid())) {
            if (DEBUG_LOG) {
                Log.d(TAG, String.format("Received Text: %d", characteristic.getValue().length));
            }
            mCallBack.callbackMethod(action + EXTRA_DATA, characteristic.getValue());
        } else if (NAME_CHAR_UUID.equals(characteristic.getUuid())) {
            mCallBack.callbackMethod(action + NAME_DATA, characteristic.getValue());
        }
    }

    /**
     * Constructor.
     * @param gattCallback gattCallback
     */
    public BleDeviceService(final BleCallback gattCallback) {
        mCallBack = gattCallback;
    }

    /**
     * refreshDeviceCache.
     * @param gatt gatt
     * @return result
     */
    private boolean refreshDeviceCache(final BluetoothGatt gatt) {
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                if (DEBUG_LOG) {
                    Log.d(TAG, String.format("refreshDeviceCache : %b", bool));
                }
                return bool;
            }
        } catch (Exception localException) {
            if (DEBUG_LOG) {
                Log.e(TAG, "An exception occured while refreshing device");
            }
        }
        if (DEBUG_LOG) {
            Log.d(TAG, String.format("refreshDeviceCache : false"));
        }
        return false;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param context context
     * @param device The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final Context context, final BluetoothDevice device) {
        if (device == null) {
            if (DEBUG_LOG) {
                Log.w(TAG, "Device not found. Unable to connect.");
            }
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && device.getAddress().equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            if (DEBUG_LOG) {
                Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            }
            if (mBluetoothGatt.connect()) {
                setmConnectionState(STATE_CONNECTING);
                return true;
            } else {
                return false;
            }
        }

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
        refreshDeviceCache(mBluetoothGatt);
        if (DEBUG_LOG) {
            Log.d(TAG, "Trying to create a new connection.");
        }
        mBluetoothDeviceAddress = device.getAddress();
        setmConnectionState(STATE_CONNECTING);
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothGatt == null) {
            if (DEBUG_LOG) {
                Log.w(TAG, "mBluetoothGatt not initialized");
            }
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        if (DEBUG_LOG) {
            Log.w(TAG, "mBluetoothGatt closed");
        }
        mBluetoothDeviceAddress = null;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the 
     * {@code BluetoothGattCallback#onCharacteristicRead
     * (android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(final BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt == null) {
            if (DEBUG_LOG) {
                Log.w(TAG, "mBluetoothGatt not initialized");
            }
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * writeTXCharacteristic.
     * @param value value
     */
    public void writeTXCharacteristic(final byte[] value) {
    	BluetoothGattCharacteristic rxChar;
        BluetoothGattService rxService = mBluetoothGatt.getService(RX_SERVICE_UUID2);
        if (rxService == null) {
            showMessage("mBluetoothGatt null" + mBluetoothGatt);
            showMessage("Tx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        rxChar = rxService.getCharacteristic(RX_CHAR_UUID2);
        if (rxChar == null) {
            showMessage("Tx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        if (DEBUG_LOG) {
            Log.w(TAG, "TxChar = " + RX_CHAR_UUID2.toString());
        }
        rxChar.setValue(value);
        boolean status = mBluetoothGatt.writeCharacteristic(rxChar);
        if (DEBUG_LOG) {
            Log.d(TAG, "write TXchar - status=" + status);
        }
    }

    /**
     * readNameCharacteristic.
     */
    public void readNameCharacteristic() {
        BluetoothGattService rxService = mBluetoothGatt.getService(RX_SERVICE_UUID2);
        if (rxService == null) {
            showMessage("Rx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        BluetoothGattCharacteristic nameChar = rxService.getCharacteristic(NAME_CHAR_UUID);
        if (nameChar == null) {
            showMessage("Name charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        boolean status = mBluetoothGatt.readCharacteristic(nameChar);
        if (DEBUG_LOG) {
            Log.d(TAG, "read NAMEchar - status=" + status);
        }
    }

    /**
     * writeNameCharacteristic.
     * @param value value
     */
    public void writeNameCharacteristic(final byte[] value) {
        BluetoothGattService rxService = mBluetoothGatt.getService(RX_SERVICE_UUID2);
        if (rxService == null) {
            showMessage("Rx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        BluetoothGattCharacteristic nameChar = rxService.getCharacteristic(NAME_CHAR_UUID);
        if (nameChar == null) {
            showMessage("Rx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        nameChar.setValue(value);
        boolean status = mBluetoothGatt.writeCharacteristic(nameChar);
        if (DEBUG_LOG) {
            Log.d(TAG, "write NAMEchar - status=" + status);
        }
    }

    /**
     * showMessage.
     * @param msg msg
     */
    private void showMessage(final String msg) {
        if (DEBUG_LOG) {
            Log.e(TAG, msg);
        }
    }

    /**
     * getmConnectionState.
     * @return ConnectionState
     */
    public int getmConnectionState() {
        return mConnectionState;
    }

    /**
     * setmConnectionState.
     * @param connectionState connectionState
     */
    public void setmConnectionState(final int connectionState) {
        this.mConnectionState = connectionState;
    }
}
