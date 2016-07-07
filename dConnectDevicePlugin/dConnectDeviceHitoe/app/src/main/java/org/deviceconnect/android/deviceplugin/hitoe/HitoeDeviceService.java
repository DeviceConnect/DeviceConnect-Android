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

import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.profile.HitoeServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.hitoe.profile.HitoeSystemProfile;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;

import java.util.List;

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
    private final HitoeManager.OnHitoeConnectionListener mOnHitoeConnectionListener = new HitoeManager.OnHitoeConnectionListener() {
        @Override
        public void onConnected(HitoeDevice device) {
            DConnectService service = new HitoeService(getManager(), device);
            getServiceProvider().addService(service);
        }

        @Override
        public void onConnectFailed(HitoeDevice device) {
        }

        @Override
        public void onDiscovery(List<HitoeDevice> devices) {
        }

        @Override
        public void onDisconnected(int res, HitoeDevice device) {
        }

    };

    @Override
    public void onCreate() {
        super.onCreate();
        HitoeApplication app = (HitoeApplication) getApplication();
        app.initialize();
        EventManager.INSTANCE.setController(new MemoryCacheController());
        getManager().addHitoeConnectionListener(mOnHitoeConnectionListener);
        registerBluetoothFilter();
        HitoeManager mgr =  getManager();
        if (mgr != null) {
            List<HitoeDevice> devices = mgr.getRegisterDevices();
            for (HitoeDevice device : devices) {
                getServiceProvider().addService(new HitoeService(mgr, device));
            }
        }
        addProfile(new HitoeServiceDiscoveryProfile(getServiceProvider()));

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
