/*
 DConnectApplication.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import org.deviceconnect.android.deviceplugin.host.HostDeviceApplication;
import android.content.Context;
import android.content.SharedPreferences;

import org.deviceconnect.android.logger.AndroidHandler;
import org.deviceconnect.android.manager.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.util.DConnectUtil;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Device Connect Manager Application.
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectApplication  extends HostDeviceApplication {
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
        Logger logger = Logger.getLogger("dconnect.manager");
        if (BuildConfig.DEBUG) {
            AndroidHandler handler = new AndroidHandler(logger.getName());
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            logger.addHandler(handler);
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
        } else {
            logger.setLevel(Level.OFF);
        }

        initialize();

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

    private void initialize() {
        SharedPreferences sp = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);

        String name = sp.getString(getString(R.string.key_settings_dconn_name), null);
        if (name == null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(getString(R.string.key_settings_dconn_name), DConnectUtil.createName());
            editor.apply();
        }

        String uuid = sp.getString(getString(R.string.key_settings_dconn_uuid), null);
        if (uuid == null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(getString(R.string.key_settings_dconn_uuid), DConnectUtil.createUuid());
            editor.apply();
        }
    }
}
