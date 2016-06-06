/*
 HitoeDeviceService
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import org.deviceconnect.android.deviceplugin.hitoe.ble.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.profile.HitoeHealthProfile;
import org.deviceconnect.android.deviceplugin.hitoe.profile.HitoeServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.hitoe.profile.HitoeServiceInformationProfile;
import org.deviceconnect.android.deviceplugin.hitoe.profile.HitoeSystemProfile;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * This service provide Hitoe Profile.
 * @author NTT DOCOMO, INC.
 */
public class HitoeDeviceService extends DConnectMessageService {
    /**
     * Instance of handler.
     */
    private final Handler mHandler = new Handler();

    /**
     * Received a event that Bluetooth has been changed.
     */
    private final BroadcastReceiver mSensorReceiver = new BroadcastReceiver() {
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

    @Override
    public void onCreate() {
        super.onCreate();
        EventManager.INSTANCE.setController(new MemoryCacheController());

        HitoeApplication app = (HitoeApplication) getApplication();
        app.initialize();

        addProfile(new HitoeHealthProfile());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBluetoothFilter();
        getManager().stop();

    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new HitoeSystemProfile();
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new HitoeServiceDiscoveryProfile(this);
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new HitoeServiceInformationProfile(this);
    }
    /**
     * Register a BroadcastReceiver of Bluetooth event.
     */
    private void registerBluetoothFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mSensorReceiver, filter, null, mHandler);
    }
    /**
     * Unregister a previously registered BroadcastReceiver.
     */
    private void unregisterBluetoothFilter() {
        unregisterReceiver(mSensorReceiver);
    }

    /**
     * Gets a instance of HeartRateManager.
     *
     * @return HeartRateManager
     */
    private HitoeManager getManager() {
        HitoeApplication app = (HitoeApplication) getApplication();
        return app.getHitoeManager();
    }
}
