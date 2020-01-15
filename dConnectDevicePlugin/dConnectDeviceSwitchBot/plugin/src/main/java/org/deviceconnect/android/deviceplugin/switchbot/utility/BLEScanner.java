package org.deviceconnect.android.deviceplugin.switchbot.utility;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;

public class BLEScanner {
    private static final ParcelUuid SWITCHBOT_BLE_GATT_SERVICE_UUID = ParcelUuid.fromString("cba20d00-224d-11e6-9fb8-0002a5d5c51b");
    private static final int SCAN_PERIOD_MILLISECONDS = 30000;
    private EventListener eventListener;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback scanCallback = new ScanCallback() {
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }

        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result != null) {
                eventListener.onDetectDevice(result.getDevice());
            }
        }
    };
    private ArrayList<ScanFilter> scanFilterList = new ArrayList<>();
    private ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
    private Boolean isScanning = false;
    private final Object lock = new Object();

    public BLEScanner(Context context, EventListener eventListener) {
        this.eventListener = eventListener;
        scanFilterList.add(new ScanFilter.Builder().setServiceUuid(SWITCHBOT_BLE_GATT_SERVICE_UUID).build());
        BluetoothManager bluetoothManager = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE));
        if (bluetoothManager != null) {
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
    }

    public void startScan() {
        synchronized (lock) {
            if (!isScanning) {
                isScanning = true;
                bluetoothLeScanner.startScan(scanFilterList, scanSettings, scanCallback);
                new Handler(Looper.getMainLooper()).postDelayed(this::stopScan, SCAN_PERIOD_MILLISECONDS);
            }
        }
    }

    public void startScan(final String deviceAddress) {
        synchronized (lock) {
            if (!isScanning) {
                isScanning = true;
                scanFilterList.add(new ScanFilter.Builder().setDeviceAddress(deviceAddress).build());
                bluetoothLeScanner.startScan(scanFilterList, scanSettings, scanCallback);
                new Handler(Looper.getMainLooper()).postDelayed(this::stopScan, SCAN_PERIOD_MILLISECONDS);
            }
        }
    }

    public void stopScan() {
        synchronized (lock) {
            if (isScanning) {
                bluetoothLeScanner.stopScan(scanCallback);
            }
        }
    }

    public interface EventListener {
        void onDetectDevice(BluetoothDevice bluetoothDevice);
    }
}
