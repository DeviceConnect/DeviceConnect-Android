/*
 HeartRateDeviceService
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import org.deviceconnect.android.deviceplugin.heartrate.ble.BleUtils;
import org.deviceconnect.android.deviceplugin.heartrate.profile.HeartRateHealthProfile;
import org.deviceconnect.android.deviceplugin.heartrate.profile.HeartRateServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.heartrate.profile.HeartRateSystemProfile;
import org.deviceconnect.android.deviceplugin.heartrate.service.HeartRateService;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceProvider;

import java.util.List;
import java.util.logging.Logger;

/**
 * This service provide Health Profile.
 * @author NTT DOCOMO, INC.
 */
public class HeartRateDeviceService extends DConnectMessageService {
    /** Logger. */
    private final Logger mLogger = Logger.getLogger("heartrate.dplugin");

    private DConnectProfile mHeartRateProfile;

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
                    getManager().addOnHeartRateDiscoveryListener(mOnDiscoveryListener);
                } else if (state == BluetoothAdapter.STATE_OFF) {
                    getManager().stop();
                }
            }
        }
    };

    private final HeartRateManager.OnHeartRateDiscoveryListener mOnDiscoveryListener
        = new HeartRateManager.OnHeartRateDiscoveryListener() {
            @Override
            public void onDiscovery(final List<BluetoothDevice> devices) {
                DConnectServiceProvider provider = getServiceProvider();
                for (BluetoothDevice device : devices) {
                    if (provider.getService(device.getAddress()) == null) {
                        DConnectService service = new HeartRateService(device);
                        service.addProfile(mHeartRateProfile);
                        provider.addService(service);
                    }
                }
            }

            @Override
            public void onConnected(final BluetoothDevice device) {
                DConnectService service = getServiceProvider().getService(device.getAddress());
                if (service != null) {
                    service.setOnline(true);
                }
            }

            @Override
            public void onConnectFailed(final BluetoothDevice device) {
                // NOP.
            }

            @Override
            public void onDisconnected(final BluetoothDevice device) {
                DConnectService service = getServiceProvider().getService(device.getAddress());
                if (service != null) {
                    service.setOnline(false);
                }
            }
        };

    /**
     * Instance of handler.
     */
    private final Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();

        mLogger.fine("HeartRateDeviceService start.");

        if (!BleUtils.isBLESupported(getContext())) {
            mLogger.warning("BLE is not support.");
            return;
        }

        EventManager.INSTANCE.setController(new MemoryCacheController());

        HeartRateApplication app = (HeartRateApplication) getApplication();
        app.initialize();

        getManager().addOnHeartRateDiscoveryListener(mOnDiscoveryListener);

        addProfile(new HeartRateServiceDiscoveryProfile(getServiceProvider()));
        mHeartRateProfile = new HeartRateHealthProfile(app.getHeartRateManager());

        registerBluetoothFilter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBluetoothFilter();
        getManager().removeOnHeartRateDiscoveryListener(mOnDiscoveryListener);
        getManager().stop();
        mLogger.fine("HeartRateDeviceService end.");
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new HeartRateSystemProfile();
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
    private HeartRateManager getManager() {
        HeartRateApplication app = (HeartRateApplication) getApplication();
        return app.getHeartRateManager();
    }
}
