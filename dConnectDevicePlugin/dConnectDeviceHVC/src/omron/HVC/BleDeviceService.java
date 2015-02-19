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

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BleDeviceService {
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
	public final static String NAME_DATA = ":NAME_DATA";
    public final static String EXTRA_DATA = ":EXTRA_DATA";
    public final static String DEVICE_DOES_NOT_SUPPORT_UART = "DEVICE_DOES_NOT_SUPPORT_UART";

    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	public static final UUID RX_SERVICE_UUID2 = UUID.fromString("35100001-d13a-4f39-8ab3-bf64d4fbb4b4");
	public static final UUID RX_CHAR_UUID2 = UUID.fromString("35100002-d13a-4f39-8ab3-bf64d4fbb4b4");
	public static final UUID TX_CHAR_UUID2 = UUID.fromString("35100003-d13a-4f39-8ab3-bf64d4fbb4b4");

	public static final UUID NAME_CHAR_UUID = UUID.fromString("35100004-d13a-4f39-8ab3-bf64d4fbb4b4");

    private BleCallback mCallBack = null;

    private BluetoothGatt mBluetoothGatt;
    private String mBluetoothDeviceAddress;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final String TAG = "BleDeviceService";

	// Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                setmConnectionState(STATE_CONNECTED);
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                setmConnectionState(STATE_DISCONNECTED);
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "mBluetoothGatt = " + mBluetoothGatt );
            	BluetoothGattCharacteristic TxChar;
                BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID2);
                if (RxService == null) {
		            showMessage("mBluetoothGatt null"+ mBluetoothGatt);
					showMessage("Rx service not found!");
					broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
					return;
                }
                TxChar = RxService.getCharacteristic(TX_CHAR_UUID2);
                if (TxChar == null) {
                    showMessage("Rx charateristic not found!");
                    broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
                    return;
                }
                Log.w(TAG, "RxChar = " + TX_CHAR_UUID2.toString() );
                mBluetoothGatt.setCharacteristicNotification(TxChar,true);

                BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);

                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.d(TAG, String.format("onCharacteristicRead: %s",characteristic.getUuid()));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, String.format("onCharacteristicChanged: %s",characteristic.getUuid()));
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        mCallBack.callbackMethod(action);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (TX_CHAR_UUID2.equals(characteristic.getUuid())) {
            Log.d(TAG, String.format("Received Text: %d",characteristic.getValue().length));
	        mCallBack.callbackMethod(action+EXTRA_DATA, characteristic.getValue());
		} else
		if (NAME_CHAR_UUID.equals(characteristic.getUuid())) {
	        mCallBack.callbackMethod(action+NAME_DATA, characteristic.getValue());
		}
    }

    public BleDeviceService(BleCallback gattCallback) {
        mCallBack = gattCallback;
    }

    private boolean refreshDeviceCache(BluetoothGatt gatt){
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                Log.d(TAG, String.format("refreshDeviceCache : %d", bool));
                return bool;
            }
        }
        catch (Exception localException) {
            Log.e(TAG, "An exception occured while refreshing device");
        }
        Log.d(TAG, String.format("refreshDeviceCache : false"));
        return false;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(Context context, BluetoothDevice device) {
        if (device == null) {
            Log.w(TAG, "Device not found. Unable to connect.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && device.getAddress().equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
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
        Log.d(TAG, "Trying to create a new connection.");
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
            Log.w(TAG, "mBluetoothGatt not initialized");
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
        Log.w(TAG, "mBluetoothGatt closed");
        mBluetoothDeviceAddress = null;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt == null) {
            Log.w(TAG, "mBluetoothGatt not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void writeTXCharacteristic(byte[] value)
    {
    	BluetoothGattCharacteristic RxChar;
        BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID2);
        if (RxService == null) {
            showMessage("mBluetoothGatt null"+ mBluetoothGatt);
            showMessage("Tx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        RxChar = RxService.getCharacteristic(RX_CHAR_UUID2);
        if (RxChar == null) {
            showMessage("Tx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        Log.w(TAG, "TxChar = " + RX_CHAR_UUID2.toString() );
        RxChar.setValue(value);
		boolean status = mBluetoothGatt.writeCharacteristic(RxChar);
		Log.d(TAG, "write TXchar - status=" + status);
    }

	public void readNameCharacteristic() {
		BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID2);
		if (RxService == null) {
			showMessage("Rx service not found!");
			broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
			return;
		}
		BluetoothGattCharacteristic NameChar = RxService.getCharacteristic(NAME_CHAR_UUID);
		if (NameChar == null) {
			showMessage("Name charateristic not found!");
			broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
			return;
		}
		boolean status = mBluetoothGatt.readCharacteristic(NameChar);
		Log.d(TAG, "read NAMEchar - status=" + status);
	}

	public void writeNameCharacteristic(byte[] value) {
		BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID2);
		if (RxService == null) {
			showMessage("Rx service not found!");
			broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
			return;
		}
		BluetoothGattCharacteristic NameChar = RxService.getCharacteristic(NAME_CHAR_UUID);
		if (NameChar == null) {
			showMessage("Rx charateristic not found!");
			broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
			return;
		}
		NameChar.setValue(value);
		boolean status = mBluetoothGatt.writeCharacteristic(NameChar);
		Log.d(TAG, "write NAMEchar - status=" + status);
	}

	private void showMessage(String msg) {
		Log.e(TAG, msg);
	}

    public int getmConnectionState() {
        return mConnectionState;
    }

    public void setmConnectionState(int mConnectionState) {
        this.mConnectionState = mConnectionState;
    }
}
