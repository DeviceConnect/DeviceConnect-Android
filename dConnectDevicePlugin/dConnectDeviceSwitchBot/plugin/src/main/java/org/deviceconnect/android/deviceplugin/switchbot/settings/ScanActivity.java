package org.deviceconnect.android.deviceplugin.switchbot.settings;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.switchbot.BuildConfig;
import org.deviceconnect.android.deviceplugin.switchbot.R;
import org.deviceconnect.android.deviceplugin.switchbot.utility.ListAdapter;
import org.deviceconnect.android.deviceplugin.switchbot.utility.BLEScanner;
import org.deviceconnect.android.deviceplugin.switchbot.device.SwitchBotDevice;

import java.util.ArrayList;

public class ScanActivity extends Activity implements BLEScanner.EventListener, ListAdapter.EventListener {
    private static final String TAG = "ScanActivity";
    private static final Boolean DEBUG = BuildConfig.DEBUG;
    public static final String KEY_DEVICE_ADDRESS = "key_device_address";
    private static final int REQUEST_ENABLE_BLUETOOTH = 520;
    private BLEScanner BLEScanner;
    private ListAdapter<BluetoothDevice> listAdapter;
    private String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        setTitle("デバイススキャン");

        if(DEBUG){
            Log.d(TAG, "onCreate()");
            Log.d(TAG, "savedInstanceState : " + savedInstanceState);
        }

        BLEScanner = new BLEScanner(this, this);
        listAdapter = new ListAdapter<>(new ArrayList<>(), R.layout.list_scan_row, this);

        RecyclerView deviceList = findViewById(R.id.list_device);
        deviceList.setHasFixedSize(true);
        deviceList.setLayoutManager(new LinearLayoutManager(this));
        deviceList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        deviceList.setAdapter(listAdapter);

        BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        if(bluetoothManager != null) {
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            if(bluetoothAdapter.isEnabled()) {
                startScan();
            } else {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            Log.d(TAG, "onDestroy()");
        }
        BLEScanner.stopScan();
    }

    private void startScan() {
        if (DEBUG) {
            Log.d(TAG, "startScan()");
        }
        PermissionUtility.requestPermissions(this, new Handler(Looper.getMainLooper()), permissions, new PermissionUtility.PermissionRequestCallback(){
            @Override
            public void onSuccess() {
                BLEScanner.startScan();
            }

            @Override
            public void onFail(@NonNull String s) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(TAG, "onActivityResult()");
            Log.d(TAG, "requestCode : " + requestCode);
            Log.d(TAG, "resultCode : " + resultCode);
            Log.d(TAG, "data : " + data);
        }
        if(resultCode == RESULT_OK) {
            if(requestCode == REQUEST_ENABLE_BLUETOOTH) {
                startScan();
            }
        }

    }

    @Override
    public void onDetectDevice(BluetoothDevice bluetoothDevice) {
        if (DEBUG) {
            Log.d(TAG, "onDetectDevice()");
            Log.d(TAG, "device address : " + bluetoothDevice.getAddress());
        }
        listAdapter.add(bluetoothDevice);
    }

    @Override
    public void onItemClick(BluetoothDevice bluetoothDevice) {
        if (DEBUG) {
            Log.d(TAG, "onItemClick()");
            Log.d(TAG, "device address : " + bluetoothDevice.getAddress());
        }
        BLEScanner.stopScan();
        Intent result = new Intent();
        result.putExtra(KEY_DEVICE_ADDRESS, bluetoothDevice.getAddress());
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public void onItemClick(SwitchBotDevice switchBotDevice) {
        if (DEBUG) {
            Log.d(TAG, "onItemClick()");
            Log.d(TAG, "device name : " + switchBotDevice.getDeviceName());
            Log.d(TAG, "device address : " + switchBotDevice.getDeviceAddress());
            Log.d(TAG, "device mode : " + switchBotDevice.getDeviceMode());
        }
    }
}
