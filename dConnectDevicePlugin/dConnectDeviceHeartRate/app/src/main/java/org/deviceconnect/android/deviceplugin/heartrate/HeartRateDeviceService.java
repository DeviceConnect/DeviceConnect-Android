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
import org.deviceconnect.android.deviceplugin.heartrate.data.HeartRateDevice;
import org.deviceconnect.android.deviceplugin.heartrate.profile.HeartRateHealthProfile;
import org.deviceconnect.android.deviceplugin.heartrate.profile.HeartRateServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.heartrate.profile.HeartRateSystemProfile;
import org.deviceconnect.android.deviceplugin.heartrate.service.HeartRateService;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.logger.AndroidHandler;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceListener;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * This service provide Health Profile.
 * @author NTT DOCOMO, INC.
 */
public class HeartRateDeviceService extends DConnectMessageService
    implements DConnectServiceListener {

    /** Logger. */
    private final Logger mLogger = Logger.getLogger("heartrate.dplugin");

    private DConnectProfile mHeartRateProfile;

    /**
     * Instance of HeartRateManager.
     */
    private HeartRateManager mHeartRateManager;

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

    private final HeartRateManager.OnHeartRateDiscoveryListener mOnDiscoveryListener
        = new HeartRateManager.OnHeartRateDiscoveryListener() {
            @Override
            public void onDiscovery(final List<BluetoothDevice> devices) {
                for (BluetoothDevice device : devices) {
                    retrieveService(device);
                }
            }

            @Override
            public void onConnected(final HeartRateDevice device) {
                DConnectService service = retrieveService(device);
                if (service != null) {
                    service.setOnline(true);
                }
            }

            @Override
            public void onConnectFailed(final BluetoothDevice device) {
                // NOP.
            }

            @Override
            public void onDisconnected(final HeartRateDevice device) {
                DConnectService service = retrieveService(device);
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

        Logger logger = Logger.getLogger("heartrate.dplugin");
        if (BuildConfig.DEBUG) {
            AndroidHandler handler = new AndroidHandler(logger.getName());
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            logger.addHandler(handler);
            logger.setLevel(Level.ALL);
        } else {
            logger.setLevel(Level.OFF);
        }

        mLogger.fine("HeartRateDeviceService start.");

        if (!BleUtils.isBLESupported(this)) {
            mLogger.warning("BLE is not support.");
            return;
        }

        mHeartRateManager = new HeartRateManager(getApplicationContext());
        mHeartRateManager.addOnHeartRateDiscoveryListener(mOnDiscoveryListener);

        addProfile(new HeartRateServiceDiscoveryProfile(getServiceProvider()));
        mHeartRateProfile = new HeartRateHealthProfile(mHeartRateManager);

        List<HeartRateDevice> devices = mHeartRateManager.getRegisterDevices();
        for (HeartRateDevice device : devices) {
            retrieveService(device);
        }

        getServiceProvider().addServiceListener(this);

        registerBluetoothFilter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBluetoothFilter();
        if (getPluginContext() != null) {
            getServiceProvider().removeServiceListener(this);
        }
        mHeartRateManager.removeOnHeartRateDiscoveryListener(mOnDiscoveryListener);
        mHeartRateManager.stop();
        mLogger.fine("HeartRateDeviceService end.");
    }

    @Override
    public void onServiceAdded(final DConnectService service) {
        if (BuildConfig.DEBUG) {
            mLogger.info("onServiceAdded: " + service.getName());
        }
        // NOP.
    }

    @Override
    public void onServiceRemoved(final DConnectService service) {
        if (BuildConfig.DEBUG) {
            mLogger.info("onServiceRemoved: " + service.getName());
        }
        getManager().disconnectBleDevice(service.getId());
    }

    @Override
    public void onStatusChange(final DConnectService service) {
        if (BuildConfig.DEBUG) {
            mLogger.info("onStatusChange: " + service.getName());
        }
        // NOP.
    }

    @Override
    protected void onManagerUninstalled() {
        // Managerアンインストール検知時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerUninstalled");
        }
    }

    @Override
    protected void onManagerTerminated() {
        // Manager正常終了通知受信時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerTerminated");
        }
    }

    @Override
    protected void onManagerEventTransmitDisconnected(final String sessionKey) {
        // ManagerのEvent送信経路切断通知受信時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerEventTransmitDisconnected");
        }
        if (sessionKey != null) {
            EventManager.INSTANCE.removeEvents(sessionKey);
        } else {
            EventManager.INSTANCE.removeAll();
        }
    }

    @Override
    protected void onDevicePluginReset() {
        // Device Plug-inへのReset要求受信時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onDevicePluginReset");
        }
        resetPluginResource();
    }

    /**
     * リソースリセット処理.
     */
    private void resetPluginResource() {
        // 全イベント削除.
        EventManager.INSTANCE.removeAll();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new HeartRateSystemProfile();
    }

    /**
     * Get a DConnectService from HeartRateDevice.
     * @param device HeartRateDevice
     * @return DConnectService
     */
    private DConnectService retrieveService(final HeartRateDevice device) {
        DConnectService service = getServiceProvider().getService(device.getAddress());
        if (service == null) {
            service = new HeartRateService(device);
            service.addProfile(mHeartRateProfile);
            getServiceProvider().addService(service);
        }
        return service;
    }

    /**
     * Get a DConnectService from BluetoothDevice.
     * @param device BluetoothDevice
     * @return DConnectService
     */
    private DConnectService retrieveService(final BluetoothDevice device) {
        DConnectService service = getServiceProvider().getService(device.getAddress());
        if (service == null) {
            service = new HeartRateService(device);
            service.addProfile(mHeartRateProfile);
            getServiceProvider().addService(service);
        }
        return service;
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
    public HeartRateManager getManager() {
        return mHeartRateManager;
    }
}
