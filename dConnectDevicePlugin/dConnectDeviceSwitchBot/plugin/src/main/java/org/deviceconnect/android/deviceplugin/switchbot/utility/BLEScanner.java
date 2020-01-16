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
import android.util.Log;

import org.deviceconnect.android.deviceplugin.switchbot.BuildConfig;

import java.util.ArrayList;
import java.util.List;

public class BLEScanner {
    private static final String TAG = "BLEScanner";
    private static final Boolean DEBUG = BuildConfig.DEBUG;
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
    private ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
    private Boolean isScanning = false;
    private final Object lock = new Object();

    public BLEScanner(EventListener eventListener) {
        this.eventListener = eventListener;
        scanFilterList.add(new ScanFilter.Builder().setServiceUuid(SWITCHBOT_BLE_GATT_SERVICE_UUID).build());
    }

    public void startScan(final Context context) {
        if(DEBUG){
            Log.d(TAG, "startScan()");
            Log.d(TAG, "context : " + context);
            Log.d(TAG, "isScanning : " + isScanning);
            Log.d(TAG, "bluetoothLeScanner : " + bluetoothLeScanner);
            Log.d(TAG, "scanFilterList : " + scanFilterList);
            Log.d(TAG, "scanSettings : " + scanSettings);
            Log.d(TAG, "scanCallback : " + scanCallback);
        }
        synchronized (lock) {
            BluetoothManager bluetoothManager = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE));
            if (bluetoothManager != null) {
                BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                if (!isScanning) {
                    isScanning = true;
                    bluetoothLeScanner.startScan(scanFilterList, scanSettings, scanCallback);
                    new Handler(Looper.getMainLooper()).postDelayed(this::stopScan, SCAN_PERIOD_MILLISECONDS);
                }
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
