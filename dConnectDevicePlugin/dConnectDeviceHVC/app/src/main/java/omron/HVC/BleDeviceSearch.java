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

import java.util.ArrayList;
import java.util.List;

import org.deviceconnect.android.activity.PermissionUtility;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

/**
Bluetooth Device Search
*/
public class BleDeviceSearch {
    private List<BluetoothDevice> deviceList = null;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothReceiver mBluetoothReceiver = null;

    public BleDeviceSearch(Context context, final int searchTime) {
    	deviceList = new ArrayList<>();

    	// Step 1: Enable Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            // Bluetooth supported
            if (mBluetoothAdapter.isEnabled()) {
            	discoverDevices(context, searchTime);
            }
        }
    }

    private void discoverDevices(final Context context, final int searchTime) {
        PermissionUtility.requestPermissions(context, new Handler(Looper.getMainLooper()),
                new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                new PermissionUtility.PermissionRequestCallback() {
                    @Override
                    public void onSuccess() {
                        // Step 4: Scan for Bluetooth device
                        mBluetoothReceiver = new BluetoothReceiver();
                        context.registerReceiver(mBluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
                        mBluetoothAdapter.startDiscovery();

//        sleep(10000);
                        sleep(searchTime);

                        mBluetoothAdapter.cancelDiscovery();
                        context.unregisterReceiver(mBluetoothReceiver);
                    }

                    @Override
                    public void onFail(@NonNull String deniedPermission) {

                    }
                });
    }

    class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                deviceList.add(device);
            }
        }
    }

    protected synchronized void sleep(long msec) {
        //Method to stop execution after set number of msec
        try {
            wait(msec);
        } catch(InterruptedException e){}
    }

    public List<BluetoothDevice> getDevices() {
        // TODO Auto-generated method stub
        return deviceList;
    }
}
