package org.deviceconnect.android.deviceplugin.switchbot.device;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.switchbot.BuildConfig;

import java.util.List;
import java.util.UUID;

public class SwitchBotDevice {
    private static final String TAG = "SwitchBotDevice";
    private static final Boolean DEBUG = BuildConfig.DEBUG;
    private static final UUID SWITCHBOT_BLE_GATT_SERVICE_UUID = UUID.fromString("cba20d00-224d-11e6-9fb8-0002a5d5c51b");
    private static final UUID SWITCHBOT_BLE_GATT_CHARACTERISTIC_UUID = UUID.fromString("cba20002-224d-11e6-9fb8-0002a5d5c51b");
    private static final byte[] READ_SETTINGS_COMMAND = { 0x57, 0x02 };

    /**
     * デバイスが実施している操作
     */
    public enum Command {
        NONE,           //何もなし
        READ_SETTINGS,  //接続時の設定読み出し
        WRITE_SETTINGS, //モード変更
        OTHERS,         //その他
    }

    /**
     * デバイスの動作モード
     */
    public enum Mode {
        BUTTON(0), SWITCH(1);
        private int value;

        Mode(int value) {
            this.value = value;
        }

        public static Mode getInstance(int id) {
            if (id == 0) {
                return BUTTON;
            } else if (id == 1) {
                return SWITCH;
            } else {
                throw new RuntimeException();
            }
        }

        public int getValue() {
            return value;
        }
    }

    private String deviceName;
    private String deviceAddress;
    private Mode deviceMode;
    private Context context;
    private BluetoothGatt gatt;
    private BluetoothGattService service;
    private Command command = Command.NONE;

    public SwitchBotDevice(Context context, final String deviceName, final String deviceAddress, Mode deviceMode) {
        if(DEBUG){
            Log.d(TAG, "SwitchBotDevice()");
            Log.d(TAG, "context:" + context);
            Log.d(TAG, "device name:" + deviceName);
            Log.d(TAG, "device address:" + deviceAddress);
            Log.d(TAG, "device mode:" + deviceMode);
        }
        this.context = context;
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
        this.deviceMode = deviceMode;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public Mode getDeviceMode() {
        return deviceMode;
    }

    /**
     * デバイスと接続する
     */
    public void connect() {
        if(DEBUG){
            Log.d(TAG, "connect()");
        }
        if(command != Command.NONE) {
            Log.e(TAG, "device busy");
            return;
        }
        if(context != null) {
            BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
            if(bluetoothManager != null) {
                BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                if(bluetoothAdapter != null) {
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                    if(device != null) {
                        //暫定的に再接続ON
                        device.connectGatt(context, true, new BluetoothGattCallback() {
                            @Override
                            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                                super.onConnectionStateChange(gatt, status, newState);
                                if(DEBUG){
                                    Log.d(TAG, "onConnectionStateChange()");
                                    Log.d(TAG, "gatt : " + gatt);
                                    Log.d(TAG, "status : " + status);
                                    Log.d(TAG, "newState : " + newState);
                                }
                                if(gatt != null) {
                                    if(newState == BluetoothGatt.STATE_CONNECTED) {
                                        SwitchBotDevice.this.gatt = gatt;
                                        SwitchBotDevice.this.gatt.discoverServices();
                                    } else if(newState == BluetoothGatt.STATE_DISCONNECTED) {
                                        SwitchBotDevice.this.gatt = null;
                                        SwitchBotDevice.this.service = null;
                                    }
                                }
                            }

                            @Override
                            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                                super.onServicesDiscovered(gatt, status);
                                if(DEBUG){
                                    Log.d(TAG, "onServicesDiscovered()");
                                    Log.d(TAG, "gatt : " + gatt);
                                    Log.d(TAG, "status : " + status);
                                }
                                service = SwitchBotDevice.this.gatt.getService(SWITCHBOT_BLE_GATT_SERVICE_UUID);
                                if(service != null) {
                                    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                                    if(characteristics != null) {
                                        for (BluetoothGattCharacteristic bluetoothGattCharacteristic : characteristics) {
                                            if(DEBUG){
                                                Log.d(TAG, "UUID(characteristic) : " + bluetoothGattCharacteristic.getUuid().toString());
                                            }
                                            List<BluetoothGattDescriptor> bluetoothGattDescriptors = bluetoothGattCharacteristic.getDescriptors();
                                            if(bluetoothGattDescriptors != null) {
                                                for(BluetoothGattDescriptor bluetoothGattDescriptor : bluetoothGattDescriptors) {
                                                    if(DEBUG){
                                                        Log.d(TAG, "UUID(descriptor) : " + bluetoothGattDescriptor.getUuid().toString());
                                                    }
                                                    SwitchBotDevice.this.gatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);
                                                    bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                                    SwitchBotDevice.this.gatt.writeDescriptor(bluetoothGattDescriptor);
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            /*@Override
                            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                                super.onCharacteristicRead(gatt, characteristic, status);
                            }*/

                            @Override
                            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                                super.onCharacteristicWrite(gatt, characteristic, status);
                                if (DEBUG) {
                                    Log.d(TAG, "onCharacteristicWrite()");
                                    Log.d(TAG, "gatt : " + gatt);
                                    Log.d(TAG, "characteristic : " + characteristic);
                                    Log.d(TAG, "status : " + status);
                                }
                            }

                            @Override
                            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                                super.onCharacteristicChanged(gatt, characteristic);
                                if (DEBUG) {
                                    Log.d(TAG, "onCharacteristicChanged()");
                                    Log.d(TAG, "gatt : " + gatt);
                                    Log.d(TAG, "characteristic : " + characteristic);
                                }
                                if(characteristic != null) {
                                    byte[] values = characteristic.getValue();
                                    if(DEBUG){
                                        for(byte it : values){
                                            Log.d(TAG, "value : " + it);
                                        }
                                    }
                                    if(DEBUG){
                                        Log.d(TAG, "command : " + command);
                                    }
                                    if(command == Command.READ_SETTINGS) {
                                        Mode mode = Mode.getInstance((values[9] >> 4) & 0x01);
                                        if(DEBUG){
                                            Log.d(TAG, "device mode(read) : " + mode);
                                            Log.d(TAG, "device mode : " + deviceMode);
                                        }
                                        if (mode != deviceMode) {
                                            modeChange();
                                        } else {
                                            command = Command.NONE;
                                        }
                                    }
                                }
                            }

                            /*@Override
                            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                                super.onDescriptorRead(gatt, descriptor, status);
                            }*/

                            @Override
                            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                                super.onDescriptorWrite(gatt, descriptor, status);
                                if (DEBUG) {
                                    Log.d(TAG, "onDescriptorWrite()");
                                    Log.d(TAG, "gatt : " + gatt);
                                    Log.d(TAG, "descriptor : " + descriptor);
                                    Log.d(TAG, "status : " + status);
                                }
                                if(gatt != null) {
                                    if(service != null) {
                                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(SWITCHBOT_BLE_GATT_CHARACTERISTIC_UUID);
                                        if(characteristic != null) {
                                            command = Command.READ_SETTINGS;
                                            characteristic.setValue(READ_SETTINGS_COMMAND);
                                            gatt.writeCharacteristic(characteristic);
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * デバイスの動作モードを変更する
     * この呼出は接続完了後の設定読み出しでモードが不一致だった場合のみ実施される
     * deviceModeに設定されたモードに変更する
     */
    private void modeChange(){
        if(DEBUG){
            Log.d(TAG,"modeChange()");
        }
        byte mode;
        if(deviceMode == Mode.BUTTON) {
            mode = 0x00;
        } else {
            mode = 0x10;
        }
        byte[] modeChangeCommand = { 0x57, 0x03, 0x64, mode };
        if(service != null && gatt != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(SWITCHBOT_BLE_GATT_CHARACTERISTIC_UUID);
            if(characteristic != null) {
                command = Command.WRITE_SETTINGS;
                characteristic.setValue(modeChangeCommand);
                gatt.writeCharacteristic(characteristic);
            }
        }
    }

    /**
     * デバイスを切断する
     */
    public void disconnect(){
        if(DEBUG){
            Log.d(TAG, "disconnect()");
        }
        if(gatt != null){
            gatt.close();
            gatt.disconnect();
        }
    }
}
