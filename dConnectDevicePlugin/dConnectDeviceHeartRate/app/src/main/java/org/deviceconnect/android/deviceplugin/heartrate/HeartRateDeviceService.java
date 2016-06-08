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

import org.deviceconnect.android.api.EndPoint;
import org.deviceconnect.android.api.EndPointManager;
import org.deviceconnect.android.deviceplugin.heartrate.ble.BleUtils;
import org.deviceconnect.android.deviceplugin.heartrate.profile.HeartRateHealthProfile;
import org.deviceconnect.android.deviceplugin.heartrate.profile.HeartRateServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.heartrate.profile.HeartRateServiceInformationProfile;
import org.deviceconnect.android.deviceplugin.heartrate.profile.HeartRateSystemProfile;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.HealthProfileConstants;

import java.util.List;
import java.util.logging.Logger;

/**
 * This service provide Health Profile.
 * @author NTT DOCOMO, INC.
 */
public class HeartRateDeviceService extends DConnectMessageService {
    /** Logger. */
    private final Logger mLogger = Logger.getLogger("heartrate.dplugin");

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
                // NOP.
            }

            @Override
            public void onConnected(final BluetoothDevice device) {
                if (!EndPointManager.INSTANCE.hasEndPoint(device.getAddress())) {
                    EndPoint endPoint = new EndPoint.Builder()
                        .addApi(DConnectMessage.METHOD_GET,
                            HealthProfileConstants.PARAM_HEART_RATE)
                        .addApi(DConnectMessage.METHOD_PUT,
                            HealthProfileConstants.PARAM_HEART_RATE)
                        .addApi(DConnectMessage.METHOD_DELETE,
                            HealthProfileConstants.PARAM_HEART_RATE)
                        .build();
                    EndPointManager.INSTANCE.addEndPoint(endPoint);
                }
            }

            @Override
            public void onConnectFailed(final BluetoothDevice device) {
                // NOP.
            }

            @Override
            public void onDisconnected(final BluetoothDevice device) {
                EndPointManager.INSTANCE.removeEndPoint(device.getAddress());
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

        addProfile(new HeartRateHealthProfile(app.getHeartRateManager()));

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

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new HeartRateServiceDiscoveryProfile(this);
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new HeartRateServiceInformationProfile(this);
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
