/*
 AbstractHOGPServer.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp.server;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelUuid;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.hogp.BuildConfig;
import org.deviceconnect.android.deviceplugin.hogp.util.BatteryUtils;
import org.deviceconnect.android.deviceplugin.hogp.util.BleUuidUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.deviceconnect.android.deviceplugin.hogp.util.BleUuidUtils.CHARACTERISTIC_BATTERY_LEVEL;
import static org.deviceconnect.android.deviceplugin.hogp.util.BleUuidUtils.CHARACTERISTIC_HID_CONTROL_POINT;
import static org.deviceconnect.android.deviceplugin.hogp.util.BleUuidUtils.CHARACTERISTIC_HID_INFORMATION;
import static org.deviceconnect.android.deviceplugin.hogp.util.BleUuidUtils.CHARACTERISTIC_MANUFACTURER_NAME;
import static org.deviceconnect.android.deviceplugin.hogp.util.BleUuidUtils.CHARACTERISTIC_MODEL_NUMBER;
import static org.deviceconnect.android.deviceplugin.hogp.util.BleUuidUtils.CHARACTERISTIC_PROTOCOL_MODE;
import static org.deviceconnect.android.deviceplugin.hogp.util.BleUuidUtils.CHARACTERISTIC_REPORT;
import static org.deviceconnect.android.deviceplugin.hogp.util.BleUuidUtils.CHARACTERISTIC_REPORT_MAP;
import static org.deviceconnect.android.deviceplugin.hogp.util.BleUuidUtils.CHARACTERISTIC_SERIAL_NUMBER;
import static org.deviceconnect.android.deviceplugin.hogp.util.BleUuidUtils.DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION;
import static org.deviceconnect.android.deviceplugin.hogp.util.BleUuidUtils.DESCRIPTOR_REPORT_REFERENCE;
import static org.deviceconnect.android.deviceplugin.hogp.util.BleUuidUtils.DEVICE_INFO_MAX_LENGTH;
import static org.deviceconnect.android.deviceplugin.hogp.util.BleUuidUtils.SERVICE_BATTERY;
import static org.deviceconnect.android.deviceplugin.hogp.util.BleUuidUtils.SERVICE_BLE_HID;
import static org.deviceconnect.android.deviceplugin.hogp.util.BleUuidUtils.SERVICE_DEVICE_INFORMATION;

/**
 * HOGPの挙動を行うためのGattServerを実装したクラス.
 *
 * @author NTT DOCOMO, INC.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public abstract class AbstractHOGPServer {
    /**
     * デバッグ用フラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "HOGP";

    /**
     * このインスタンスが属するコンテキスト.
     */
    private final Context mApplicationContext;

    /**
     * アドバタイズを行うクラス.
     */
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    /**
     * Inputレポートキャラクタリスティック.
     */
    private BluetoothGattCharacteristic mInputReportCharacteristic;

    /**
     * GattServerのインスタンス.
     */
    private BluetoothGattServer mGattServer;

    /**
     * 接続したBluetoothデバイス.
     */
    private final Map<String, BluetoothDevice> mBluetoothDevicesMap = new HashMap<>();

    /**
     * マニファクチャー名.
     */
    private byte[] mManufacturerName;

    /**
     * デバイス名.
     */
    private byte[] mDeviceName;

    /**
     * シリアルナンバー.
     */
    private byte[] mSerialNumber;

    /**
     * HID Input Report
     */
    private final Queue<ReportHolder> mInputReportQueue = new ConcurrentLinkedQueue<>();

    /**
     * Bluetoothデバイスの処理を同期的に処理するためのハンドラ.
     */
    private Handler mHandler;

    /**
     * ハンドラ用のスレッド.
     */
    private HandlerThread mHandlerThread;

    /**
     * デバイスに送信するタイマー.
     */
    private Timer mTimer;

    /**
     * デバイスにHIDを送る送信レート.
     */
    private int mDataSendingRate = 10;

    /**
     * デバイス接続イベントを通知するリスナー.
     */
    private OnHOGPServerListener mOnHOGPServerListener;

    /**
     * 空のバイト配列.
     */
    private static final byte[] EMPTY_BYTES = {};

    /**
     * HIDインフォメーションのレスポンス.
     */
    private static final byte[] RESPONSE_HID_INFORMATION = {0x11, 0x01, 0x00, 0x03};

    /**
     * コンストラクタ.
     * @param context このインスタンスが属するコンテキスト
     * @throws UnsupportedOperationException BluetoothやBLEがサポートされていない場合に発生
     */
    AbstractHOGPServer(final Context context) throws UnsupportedOperationException {
        mApplicationContext = context.getApplicationContext();

        final BluetoothManager btMgr = (BluetoothManager) mApplicationContext.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter btAdapter = btMgr.getAdapter();
        if (btAdapter == null) {
            throw new UnsupportedOperationException("Bluetooth is not available.");
        }

        if (!btAdapter.isEnabled()) {
            throw new UnsupportedOperationException("Bluetooth is disabled.");
        }

        if (!btAdapter.isMultipleAdvertisementSupported()) {
            throw new UnsupportedOperationException("Bluetooth LE Advertising not supported on this device.");
        }

        mBluetoothLeAdvertiser = btAdapter.getBluetoothLeAdvertiser();
        if (mBluetoothLeAdvertiser == null) {
            throw new UnsupportedOperationException("Bluetooth LE Advertising not supported on this device.");
        }
    }

    /**
     * HIDデータ送信レートを設定します.
     * @param dataSendingRate 送信レート
     */
    public void setDataSendingRate(final int dataSendingRate) {
        mDataSendingRate = dataSendingRate;
    }

    /**
     * OnHOGPServerListenerを設定します.
     * @param listener リスナー
     */
    public void setOnHOGPServerListener(final OnHOGPServerListener listener) {
        mOnHOGPServerListener = listener;
    }

    /**
     * Manufacturer名を設定します.
     *
     * @param name Manufacturer名
     */
    public void setManufacturerName(final String name) {
        mManufacturerName = convertString2Bytes(name);
    }

    /**
     * デバイス名を設定します.
     *
     * @param name the name
     */
    public void setDeviceName(final String name) {
        mDeviceName = convertString2Bytes(name);
    }

    /**
     * シリアル番号を設定します.
     *
     * @param serialNumber the number
     */
    public final void setSerialNumber(final String serialNumber) {
        mSerialNumber = convertString2Bytes(serialNumber);
    }

    /**
     * 文字列をバイト配列に変換します.
     * @param name 変換する文字列
     * @return 変換されたバイト配列
     */
    private byte[] convertString2Bytes(final String name) {
        if (name == null) {
            return new byte[0];
        }

        final byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        if (nameBytes.length > DEVICE_INFO_MAX_LENGTH) {
            final byte[] bytes = new byte[DEVICE_INFO_MAX_LENGTH];
            System.arraycopy(nameBytes, 0, bytes, 0, DEVICE_INFO_MAX_LENGTH);
            return bytes;
        } else {
            return nameBytes;
        }
    }

    /**
     * レポートマップのキャラクタリスティックの値を取得します.
     * <p>
     * ここで返却するレポートマップがHOGPの機能になります。
     * </p>
     * @return Report Map data
     */
    abstract byte[] getReportMap();

    /**
     * HID Outputレポートを通知します.
     *
     * @param outputReport the report data
     */
    abstract void onOutputReport(final byte[] outputReport);

    /**
     * 送信するHIDデータを追加します.
     * <p>
     * deviceにnullが指定された場合には接続されているデバイス全てに送信します。
     * </p>
     * @param device 送信先のBluetoothデバイス
     * @param inputReport 追加するデータ
     */
    final void addInputReport(final BluetoothDevice device, final byte[] inputReport) {
        if (inputReport != null && inputReport.length > 0) {
            mInputReportQueue.offer(new ReportHolder(device, inputReport));
        }
    }

    /**
     * HOGPサーバを開始します.
     */
    public void start() {

        if (mGattServer != null) {
            if (DEBUG) {
                Log.w(TAG, "HOGP Server is already running.");
            }
            return;
        }

        BluetoothManager btMgr = (BluetoothManager) mApplicationContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mGattServer = btMgr.openGattServer(mApplicationContext, mGattServerCallback);
        if (mGattServer == null) {
            throw new UnsupportedOperationException("mGattServer is null, check Bluetooth is ON.");
        }

        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        new Thread(new Runnable() {
            @Override
            public void run() {
                addService(setUpHidService(true, true, false));
                addService(setUpDeviceInformationService());
                addService(setUpBatteryService());
            }
        }).start();

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                final ReportHolder holder = mInputReportQueue.poll();
                if (holder != null && mInputReportCharacteristic != null) {
                    mInputReportCharacteristic.setValue(holder.getReport());
                    mHandler.post(new Runnable() {
                        private void notifyCharacteristic(final BluetoothDevice device) {
                            try {
                                if (mGattServer != null) {
                                    mGattServer.notifyCharacteristicChanged(device, mInputReportCharacteristic, false);
                                }
                            } catch (final Throwable ignored) {
                                // do nothing.
                            }
                        }
                        @Override
                        public void run() {
                            if (holder.getDevice() != null) {
                                notifyCharacteristic(holder.getDevice());
                            } else {
                                Set<BluetoothDevice> devices = getDevices();
                                for (BluetoothDevice device : devices) {
                                    notifyCharacteristic(device);
                                }
                            }
                        }
                    });
                }
            }
        }, 0, mDataSendingRate);

        startAdvertising();
    }

    /**
     * HOGPサーバを停止します.
     */
    public void stop() {
        stopAdvertising();
    }

    /**
     * BluetoothGattServerにサービスを追加します.
     *
     * @param service 追加するサービス
     */
    private void addService(final BluetoothGattService service) {
        boolean serviceAdded = false;
        while (!serviceAdded) {
            try {
                // 連続でserviceを追加すると例外が発生する
                // 回避するためにsleepを入れています。
                Thread.sleep(500);
                serviceAdded = mGattServer.addService(service);
            } catch (final Exception e) {
                if (DEBUG) {
                    Log.w(TAG, "Adding Service failed", e);
                }
            }
        }
        if (DEBUG) {
            Log.d(TAG, "Service: " + service.getUuid() + " added.");
        }
    }

    /**
     * Device Informationサービスを設定します.
     *
     * @return Device Information サービスを
     */
    private BluetoothGattService setUpDeviceInformationService() {
        final BluetoothGattService service = new BluetoothGattService(SERVICE_DEVICE_INFORMATION, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        {
            final BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                    CHARACTERISTIC_MANUFACTURER_NAME,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED);
            service.addCharacteristic(characteristic);
        }
        {
            final BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                    CHARACTERISTIC_MODEL_NUMBER,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED);
            service.addCharacteristic(characteristic);
        }
        {
            final BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                    CHARACTERISTIC_SERIAL_NUMBER,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED);
            service.addCharacteristic(characteristic);
        }

        return service;
    }

    /**
     * Setup Battery Service
     *
     * @return the service
     */
    private BluetoothGattService setUpBatteryService() {
        final BluetoothGattService service = new BluetoothGattService(SERVICE_BATTERY, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        // Battery Level
        final BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                CHARACTERISTIC_BATTERY_LEVEL,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED);

        final BluetoothGattDescriptor clientCharacteristicConfigurationDescriptor = new BluetoothGattDescriptor(
                DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION,
                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);
        clientCharacteristicConfigurationDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        characteristic.addDescriptor(clientCharacteristicConfigurationDescriptor);

        service.addCharacteristic(characteristic);

        return service;
    }

    /**
     * HIDサービスを設定します.
     *
     * @param isNeedInputReport true: serves 'Input Report' BLE characteristic
     * @param isNeedOutputReport true: serves 'Output Report' BLE characteristic
     * @param isNeedFeatureReport true: serves 'Feature Report' BLE characteristic
     * @return HIDサービス
     */
    private BluetoothGattService setUpHidService(final boolean isNeedInputReport, final boolean isNeedOutputReport, final boolean isNeedFeatureReport) {
        final BluetoothGattService service = new BluetoothGattService(SERVICE_BLE_HID, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        // HID Information
        {
            final BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                    CHARACTERISTIC_HID_INFORMATION,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED);

            service.addCharacteristic(characteristic);
        }

        // Report Map
        {
            final BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                    CHARACTERISTIC_REPORT_MAP,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED);

            service.addCharacteristic(characteristic);
        }

        // Protocol Mode
        {
            final BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                    CHARACTERISTIC_PROTOCOL_MODE,
                    BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                    BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED | BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED);
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

            service.addCharacteristic(characteristic);
        }

        // HID Control Point
        {
            final BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                    CHARACTERISTIC_HID_CONTROL_POINT,
                    BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED);
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

            service.addCharacteristic(characteristic);
        }

        // Input Report
        if (isNeedInputReport) {
            final BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                    CHARACTERISTIC_REPORT,
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED | BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED);

            final BluetoothGattDescriptor clientCharacteristicConfigurationDescriptor = new BluetoothGattDescriptor(
                    DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION,
                    BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED | BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED); //  | BluetoothGattDescriptor.PERMISSION_WRITE
            clientCharacteristicConfigurationDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            characteristic.addDescriptor(clientCharacteristicConfigurationDescriptor);

            final BluetoothGattDescriptor reportReferenceDescriptor = new BluetoothGattDescriptor(
                    DESCRIPTOR_REPORT_REFERENCE,
                    BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED | BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED);
            characteristic.addDescriptor(reportReferenceDescriptor);

            service.addCharacteristic(characteristic);
            mInputReportCharacteristic = characteristic;
        }

        // Output Report
        if (isNeedOutputReport) {
            final BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                    CHARACTERISTIC_REPORT,
                    BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                    BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED | BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED);
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

            final BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(
                    DESCRIPTOR_REPORT_REFERENCE,
                    BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED | BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED);
            characteristic.addDescriptor(descriptor);

            service.addCharacteristic(characteristic);
        }

        // Feature Report
        if (isNeedFeatureReport) {
            final BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                    CHARACTERISTIC_REPORT,
                    BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED | BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED);

            final BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(
                    DESCRIPTOR_REPORT_REFERENCE,
                    BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED | BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED);
            characteristic.addDescriptor(descriptor);

            service.addCharacteristic(characteristic);
        }

        return service;
    }

    /**
     * アドバタイジングを開始します.
     */
    private void startAdvertising() {
        if (DEBUG) {
            Log.d(TAG, "startAdvertising");
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // set up advertising setting
                final AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                        .setConnectable(true)
                        .setTimeout(0)
                        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                        .build();

                // set up advertising data
                final AdvertiseData advertiseData = new AdvertiseData.Builder()
                        .setIncludeTxPowerLevel(false)
                        .setIncludeDeviceName(true)
                        .addServiceUuid(ParcelUuid.fromString(SERVICE_DEVICE_INFORMATION.toString()))
                        .addServiceUuid(ParcelUuid.fromString(SERVICE_BLE_HID.toString()))
                        .addServiceUuid(ParcelUuid.fromString(SERVICE_BATTERY.toString()))
                        .build();

                // set up scan result
                final AdvertiseData scanResult = new AdvertiseData.Builder()
                        .addServiceUuid(ParcelUuid.fromString(SERVICE_DEVICE_INFORMATION.toString()))
                        .addServiceUuid(ParcelUuid.fromString(SERVICE_BLE_HID.toString()))
                        .addServiceUuid(ParcelUuid.fromString(SERVICE_BATTERY.toString()))
                        .build();

                mBluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, scanResult, mAdvertiseCallback);
            }
        });
    }

    /**
     * アドバタイジングを停止します.
     */
    private void stopAdvertising() {
        if (DEBUG) {
            Log.d(TAG, "stopAdvertising");
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
                } catch (final IllegalStateException ignored) {
                    // BT Adapter is not turned ON
                    if (DEBUG) {
                        Log.d(TAG, "Failed to turn off advertising.", ignored);
                    }
                }

                try {
                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer = null;
                    }

                    if (mGattServer != null) {
                        final Collection<BluetoothDevice> devices = getDevices();
                        for (final BluetoothDevice device : devices) {
                            mGattServer.cancelConnection(device);
                        }

                        mGattServer.clearServices();
                        mGattServer.close();
                        mGattServer = null;
                    }
                } catch (final IllegalStateException ignored) {
                    // do nothing
                    if (DEBUG) {
                        Log.d(TAG, "Failed to stop the gatt server.", ignored);
                    }
                }

                mHandlerThread.quit();
                mHandlerThread = null;
            }
        });
    }

    /**
     * 接続中のデバイス一覧を取得します.
     *
     * @return the connected Bluetooth devices
     */
    public Set<BluetoothDevice> getDevices() {
        final Set<BluetoothDevice> deviceSet = new HashSet<>();
        synchronized (mBluetoothDevicesMap) {
            deviceSet.addAll(mBluetoothDevicesMap.values());
        }
        return Collections.unmodifiableSet(deviceSet);
    }

    /**
     * 指定されてBluetoothデバイスとペアリングを行います.
     * @param device ペアリングを行うBluetoothデバイス
     */
    private void paringDevice(final BluetoothDevice device) {
        if (DEBUG) {
            Log.i(TAG, "AbstractHOGPServer#paringDevice.");
        }

        mApplicationContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                final String action = intent.getAction();
                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    switch (state) {
                        case BluetoothDevice.BOND_BONDING:
                            if (DEBUG) {
                                Log.e(TAG, "Bond bonding.");
                            }
                            break;

                        case BluetoothDevice.BOND_BONDED:
                            BluetoothDevice bondedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            if (!bondedDevice.getAddress().equals(device.getAddress())) {
                                // 違うデバイスと接続された場合
                                if (DEBUG) {
                                    Log.w(TAG, "Connected to a different device. device=" + bondedDevice);
                                }
                                return;
                            }

                            context.unregisterReceiver(this);
                            connectDevice(device);
                            break;

                        default:
                            if (DEBUG) {
                                Log.e(TAG, "Bond error.");
                            }
                            context.unregisterReceiver(this);
                            break;
                    }
                }
            }
        }, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));

        try {
            device.setPairingConfirmation(true);
        } catch (Exception e) {
            if (DEBUG) {
                Log.d(TAG, "Failed to set pairing confirmation.", e);
            }
        }

        device.createBond();
    }

    /**
     * 指定されたBluetoothデバイスと接続を行います.
     * @param device 接続を行うBluetoothデバイス
     */
    private void connectDevice(final BluetoothDevice device) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mGattServer != null) {
                    mGattServer.connect(device, true);
                }
            }
        });

        synchronized (mBluetoothDevicesMap) {
            mBluetoothDevicesMap.put(device.getAddress(), device);
        }

        if (mOnHOGPServerListener != null) {
            mOnHOGPServerListener.onConnected(device);
        }
    }

    /**
     * 指定されたBluetoothデバイスが切断された時の処理を行います.
     *
     * @param device 切断されたデバイス
     */
    private void disconnectDevice(final BluetoothDevice device) {
        synchronized (mBluetoothDevicesMap) {
            mBluetoothDevicesMap.remove(device.getAddress());
        }

        if (mOnHOGPServerListener != null) {
            mOnHOGPServerListener.onDisconnected(device);
        }
    }

    /**
     * アドバタイジング用のコールバック.
     * <p>
     * 特に何も処理しません。
     * </p>
     */
    private final AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {};

    /**
     * BluetoothGattServerからの通知を受け取るコールバック.
     */
    private final BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {

        @Override
        public void onConnectionStateChange(final BluetoothDevice device, final int status, final int newState) {
            if (DEBUG) {
                Log.d(TAG, "onConnectionStateChange status: " + status + ", newState: " + newState);
            }

            if (mGattServer == null) {
                return;
            }

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    if (DEBUG) {
                        Log.d(TAG, "BluetoothProfile.STATE_CONNECTED bondState: " + device.getBondState());
                    }

                    // check bond status
                    if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                        paringDevice(device);
                    } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        connectDevice(device);
                    }
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    if (DEBUG) {
                        Log.w(TAG, "BluetoothProfile.STATE_DISCONNECTED");
                    }
                    disconnectDevice(device);
                    break;

                default:
                    // do nothing
                    break;
            }
        }

        @Override
        public void onCharacteristicReadRequest(final BluetoothDevice device, final int requestId, final int offset,
                                                final BluetoothGattCharacteristic characteristic) {
            if (DEBUG) {
                Log.d(TAG, "onCharacteristicReadRequest characteristic: " + characteristic.getUuid() + ", offset: " + offset);
            }

            if (mGattServer == null) {
                return;
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    final UUID characteristicUuid = characteristic.getUuid();
                    if (BleUuidUtils.matches(CHARACTERISTIC_HID_INFORMATION, characteristicUuid)) {
                        mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, RESPONSE_HID_INFORMATION);
                    } else if (BleUuidUtils.matches(CHARACTERISTIC_REPORT_MAP, characteristicUuid)) {
                        if (offset == 0) {
                            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, getReportMap());
                        } else {
                            final int remainLength = getReportMap().length - offset;
                            if (remainLength > 0) {
                                final byte[] data = new byte[remainLength];
                                System.arraycopy(getReportMap(), offset, data, 0, remainLength);
                                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, data);
                            } else {
                                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
                            }
                        }
                    } else if (BleUuidUtils.matches(CHARACTERISTIC_HID_CONTROL_POINT, characteristicUuid)) {
                        mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, new byte []{0});
                    } else if (BleUuidUtils.matches(CHARACTERISTIC_REPORT, characteristicUuid)) {
                        mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, EMPTY_BYTES);
                    } else if (BleUuidUtils.matches(CHARACTERISTIC_MANUFACTURER_NAME, characteristicUuid)) {
                        mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, mManufacturerName);
                    } else if (BleUuidUtils.matches(CHARACTERISTIC_SERIAL_NUMBER, characteristicUuid)) {
                        mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, mSerialNumber);
                    } else if (BleUuidUtils.matches(CHARACTERISTIC_MODEL_NUMBER, characteristicUuid)) {
                        mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, mDeviceName);
                    } else if (BleUuidUtils.matches(CHARACTERISTIC_BATTERY_LEVEL, characteristicUuid)) {
                        byte level = (byte) (BatteryUtils.getBatteryLevel(mApplicationContext) * 100);
                        mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, new byte[] {level});
                    } else {
                        mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, characteristic.getValue());
                    }
                }
            });
        }

        @Override
        public void onDescriptorReadRequest(final BluetoothDevice device, final int requestId, final int offset,
                                            final BluetoothGattDescriptor descriptor) {
            if (DEBUG) {
                Log.d(TAG, "onDescriptorReadRequest requestId: " + requestId + ", offset: " + offset + ", descriptor: " + descriptor.getUuid());
            }

            if (mGattServer == null) {
                return;
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (BleUuidUtils.matches(DESCRIPTOR_REPORT_REFERENCE, descriptor.getUuid())) {
                        final int property = descriptor.getCharacteristic().getProperties();
                        if (property == (BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
                            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, new byte[]{0, 1});
                        } else if (property == (BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) {
                            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, new byte[]{0, 2});
                        } else if (property == (BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE)) {
                            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, new byte[]{0, 3});
                        } else {
                            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, EMPTY_BYTES);
                        }
                    } else {
                        mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, EMPTY_BYTES);
                    }
                }
            });
        }

        @Override
        public void onCharacteristicWriteRequest(final BluetoothDevice device, final int requestId,
                                                 final BluetoothGattCharacteristic characteristic,
                                                 final boolean preparedWrite, final boolean responseNeeded,
                                                 final int offset, final byte[] value) {
            if (DEBUG) {
                Log.d(TAG, "onCharacteristicWriteRequest characteristic: " + characteristic.getUuid() + ", value: " + Arrays.toString(value));
            }

            if (mGattServer == null) {
                return;
            }

            if (responseNeeded) {
                if (BleUuidUtils.matches(CHARACTERISTIC_REPORT, characteristic.getUuid())) {
                    if (characteristic.getProperties() == (BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) {
                        onOutputReport(value);
                    }
                    mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, EMPTY_BYTES);
                } else {
                    mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, EMPTY_BYTES);
                }
            }
        }

        @Override
        public void onDescriptorWriteRequest(final BluetoothDevice device, final int requestId,
                                             final BluetoothGattDescriptor descriptor, final boolean preparedWrite,
                                             final boolean responseNeeded, final int offset, final byte[] value) {
            if (DEBUG) {
                Log.d(TAG, "onDescriptorWriteRequest descriptor: " + descriptor.getUuid() + ", value: " + Arrays.toString(value) + ", responseNeeded: " + responseNeeded + ", preparedWrite: " + preparedWrite);
            }

            if (mGattServer == null) {
                return;
            }

            descriptor.setValue(value);

            if (responseNeeded) {
                if (BleUuidUtils.matches(DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, descriptor.getUuid())) {
                    mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, EMPTY_BYTES);
                } else {
                    mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, EMPTY_BYTES);
                }
            }
        }

        @Override
        public void onServiceAdded(final int status, final BluetoothGattService service) {
            if (DEBUG) {
                Log.d(TAG, "onServiceAdded status: " + status + ", service: " + service.getUuid());
                if (status != 0) {
                    Log.d(TAG, "onServiceAdded Adding Service failed.");
                }
            }
        }
    };

    /**
     * HOGPServerにデバイスが接続・切断した時の通知を行うリスナー.
     */
    public interface OnHOGPServerListener {
        /**
         * デバイスが接続された時に呼び出すメソッド.
         * @param device 接続したデバイス
         */
        void onConnected(BluetoothDevice device);

        /**
         * デバイスが切断された時に呼び出すメソッド.
         * @param device 切断したデバイス
         */
        void onDisconnected(BluetoothDevice device);
    }

    /**
     * デバイスへのレポートを保持するクラス.
     */
    private class ReportHolder {
        /**
         * 送信先のBluetoothデバイス.
         */
        private BluetoothDevice mDevice;

        /**
         * 送信するレポート.
         */
        private byte[] mReport;

        /**
         * コンストラクタ.
         * @param device 送信先のデバイス
         * @param report 送信するレポート
         */
        ReportHolder(final BluetoothDevice device, final byte[] report) {
            mDevice = device;
            mReport = report;
        }

        /**
         * 送信先のBluetoothデバイスを取得します.
         * @return Bluetoothデバイス
         */
        BluetoothDevice getDevice() {
            return mDevice;
        }

        /**
         * 送信するレポートを取得します.
         * @return レポート
         */
        byte[] getReport() {
            return mReport;
        }
    }
}
