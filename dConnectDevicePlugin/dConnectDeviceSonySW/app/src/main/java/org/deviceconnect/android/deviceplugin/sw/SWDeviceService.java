/*
 SWService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sw;

import android.bluetooth.BluetoothDevice;

import org.deviceconnect.android.deviceplugin.bluetooth.BluetoothDeviceManager;
import org.deviceconnect.android.deviceplugin.sw.profile.SWSystemProfile;
import org.deviceconnect.android.deviceplugin.sw.service.SWService;
import org.deviceconnect.android.deviceplugin.sw.service.SWServiceFactory;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;

import java.util.logging.Logger;

/**
 * 本デバイスプラグインのプロファイルをDeviceConnectに登録するサービス.
 */
public class SWDeviceService extends DConnectMessageService {

    private final Logger mLogger = Logger.getLogger(SWConstants.LOGGER_NAME);

    private BluetoothDeviceManager mDeviceMgr;

    private final BluetoothDeviceManager.DeviceListener mDeviceListener
        = new BluetoothDeviceManager.DeviceListener() {
        @Override
        public void onFound(final BluetoothDevice smartWatch) {
            mLogger.info("onFound: name = " + smartWatch.getName());

            DConnectService service = getService(smartWatch);
            if (service == null) {
                service = SWServiceFactory.createService(smartWatch);
                getServiceProvider().addService(service);
            }
        }

        @Override
        public void onConnected(final BluetoothDevice smartWatch) {
            mLogger.info("onConnected: name = " + smartWatch.getName());
            // 接続状態は「スマートコネクト」アプリのデータベースから別途取得するため、
            // ここでは何もしない.
        }

        @Override
        public void onDisconnected(final BluetoothDevice smartWatch) {
            mLogger.info("onDisconnected: name = " + smartWatch.getName());
            // 接続状態は「スマートコネクト」アプリのデータベースから別途取得するため、
            // ここでは何もしない.
        }
    };

    private DConnectService getService(final BluetoothDevice device) {
        return getServiceProvider().getService(SWService.createServiceId(device));
    }

    @Override
    public void onCreate() {
        super.onCreate();

        EventManager.INSTANCE.setController(new MemoryCacheController());

        mDeviceMgr = new SWDeviceManager(this);
        mDeviceMgr.addDeviceListener(mDeviceListener);
        mDeviceMgr.start();
        for (BluetoothDevice smartWatch : mDeviceMgr.getCachedDeviceList()) {
            DConnectService service = SWServiceFactory.createService(smartWatch);
            getServiceProvider().addService(service);
        }
    }

    @Override
    public void onDestroy() {
        mDeviceMgr.removeDeviceListener(mDeviceListener);
        mDeviceMgr.stop();

        super.onDestroy();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new SWSystemProfile();
    }


}
