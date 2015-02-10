/*
 HeartRateDeviceService
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import org.deviceconnect.android.deviceplugin.heartrate.profile.HeartRateServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.heartrate.profile.HeartRateSystemProfile;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * @author NTT DOCOMO, INC.
 */
public class HeartRateDeviceService extends DConnectMessageService {

    private BroadcastReceiver mSensorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (state == BluetoothAdapter.STATE_ON) {
                    getManager().start();
                } else if (state == BluetoothAdapter.STATE_OFF) {
                    getManager().stop();
                }
            }
        }
    };

    private Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();

        HeartRateApplication app = (HeartRateApplication) getApplication();
        app.initialize();

        registerBluetoothFilter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBlutoothFilter();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new HeartRateSystemProfile(this);
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new HeartRateServiceDiscoveryProfile();
    }

    private void registerBluetoothFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mSensorReceiver, filter, null, mHandler);
    }

    private void unregisterBlutoothFilter() {
        unregisterReceiver(mSensorReceiver);
    }

    /**
     * Gets a instance of HeartRateManager.
     *
     * @return HeartRateManager
     */
    private HeartRateManager getManager() {
        HeartRateApplication app = (HeartRateApplication) getApplication();
        return app.getHeartRateManager();
    }
}
