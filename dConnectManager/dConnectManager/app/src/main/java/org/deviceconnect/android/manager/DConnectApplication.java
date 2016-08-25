/*
 DConnectApplication.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import android.app.Application;

/**
 * Device Connect Manager Application.
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectApplication extends Application {
    /** ドメイン名. */
    private static final String DCONNECT_DOMAIN = ".deviceconnect.org";

    /** ローカルのドメイン名. */
    private static final String LOCALHOST_DCONNECT = "localhost" + DCONNECT_DOMAIN;

    /** WebSocket管理クラス. */
    private WebSocketInfoManager mWebSocketInfoManager;

    /** デバイスプラグイン管理クラス. */
    private DevicePluginManager mDevicePluginManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mDevicePluginManager = new DevicePluginManager(this, LOCALHOST_DCONNECT);
        mDevicePluginManager.createDevicePluginList();

        mWebSocketInfoManager = new WebSocketInfoManager(this);
    }

    @Override
    public void onTerminate() {
        mWebSocketInfoManager = null;
        mDevicePluginManager = null;
        super.onTerminate();
    }

    public void updateDevicePluginList() {
        mDevicePluginManager.createDevicePluginList();
    }

    public WebSocketInfoManager getWebSocketInfoManager() {
        return mWebSocketInfoManager;
    }

    public DevicePluginManager getDevicePluginManager() {
        return mDevicePluginManager;
    }
}
