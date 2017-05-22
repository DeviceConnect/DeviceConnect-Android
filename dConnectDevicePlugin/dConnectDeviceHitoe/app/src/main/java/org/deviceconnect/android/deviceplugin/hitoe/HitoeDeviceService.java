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
import android.util.Log;

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
     * Tag.
     */
    private final String TAG = "HitoeDeviceService";
    /**
     * Instance of handler.
     */
    private final Handler mHandler = new Handler();

    /**
     * Received a event that Bluetooth has been changed.
     */
    private final BroadcastReceiver mSensorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
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
    /**
     * Connected Hitoe's info listener.
     */
    private final HitoeManager.OnHitoeConnectionListener mOnHitoeConnectionListener
            = new HitoeManager.OnHitoeConnectionListener() {
        @Override
        public void onConnected(final HitoeDevice device) {
            DConnectService service = new HitoeService(getManager(), device);
            service.setOnline(true);
            getServiceProvider().addService(service);
        }

        @Override
        public void onConnectFailed(final HitoeDevice device) {
            if (device == null) {
                return;
            }
            DConnectService service = getServiceProvider().getService(device.getId());
            if (service != null) {
                service.setOnline(false);
            }
        }

        @Override
        public void onDiscovery(final List<HitoeDevice> devices) {
            for (HitoeDevice device: devices) {
                if (device.getPinCode() != null) {
                    DConnectService service = new HitoeService(getManager(), device);
                    getServiceProvider().addService(service);
                }
            }
        }

        @Override
        public void onDisconnected(final int res, final HitoeDevice device) {
            DConnectService service = getServiceProvider().getService(device.getId());
            if (service != null) {
                service.setOnline(false);
            }
        }

        @Override
        public void onDeleted(final HitoeDevice device) {
            DConnectService service = getServiceProvider().getService(device.getId());
            if (service != null) {
                getServiceProvider().removeService(service);
            }
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
        addProfile(new HitoeServiceDiscoveryProfile(getServiceProvider()));
        HitoeManager mgr =  getManager();
        if (mgr != null) {
            List<HitoeDevice> devices = mgr.getRegisterDevices();
            for (HitoeDevice device : devices) {
                if (device.getPinCode() != null) {
                    getServiceProvider().addService(new HitoeService(mgr, device));
                }
            }
        }
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
    protected void onManagerUninstalled() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onManagerUninstalled");
        }
        getManager().stop();
        EventManager.INSTANCE.removeAll();
        removeAllServices();

    }

    @Override
    protected void onManagerTerminated() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onManagerTerminated");
        }
        EventManager.INSTANCE.removeAll();
        removeAllServices();
    }

    @Override
    protected void onManagerEventTransmitDisconnected(final String sessionKey) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onManagerEventTransmitDisconnected: " + sessionKey);
        }

        if (sessionKey != null) {
            EventManager.INSTANCE.removeEvents(sessionKey);
        } else {
            EventManager.INSTANCE.removeAll();
        }
    }

    @Override
    protected void onDevicePluginReset() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onDevicePluginReset");
        }
        resetService();
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

    /**
     * Remove All service.
     */
    private void removeAllServices() {
        List<DConnectService> services = getServiceProvider().getServiceList();
        for (DConnectService service : services) {
            getServiceProvider().removeService(service);
        }
    }

    /**
     * Reset service.
     */
    private void resetService() {
        removeAllServices();
        HitoeManager mgr =  getManager();
        if (mgr != null) {
            List<HitoeDevice> devices = mgr.getRegisterDevices();
            for (HitoeDevice device : devices) {
                if (device.getPinCode() != null) {
                    getServiceProvider().addService(new HitoeService(mgr, device));
                }
            }
        }
    }

}
