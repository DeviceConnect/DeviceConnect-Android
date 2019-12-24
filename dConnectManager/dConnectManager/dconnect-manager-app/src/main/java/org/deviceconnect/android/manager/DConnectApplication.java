/*
 DConnectApplication.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import android.content.Context;
import android.content.SharedPreferences;

import org.deviceconnect.android.deviceplugin.host.HostDeviceApplication;
import org.deviceconnect.android.logger.AndroidHandler;
import org.deviceconnect.android.manager.core.DConnectConst;
import org.deviceconnect.android.manager.core.DConnectSettings;
import org.deviceconnect.android.manager.core.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.core.util.DConnectUtil;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Device Connect Manager Application.
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectApplication extends HostDeviceApplication {

    /**
     * Device Connect システム設定.
     */
    private DConnectSettings mSettings;

    /**
     * プラグイン管理クラス.
     */
    private DevicePluginManager mPluginManager;

    @Override
    public void onCreate() {
        super.onCreate();
        setupLogger("dconnect.manager");
        setupLogger("dconnect.server");
        setupLogger("mixed-replace-media");
        setupLogger("org.deviceconnect.dplugin");
        setupLogger("org.deviceconnect.localoauth");
        setupLogger("LocalCA");

        initialize();
    }

    private void setupLogger(final String name) {
        Logger logger = Logger.getLogger(name);
        if (BuildConfig.DEBUG) {
            AndroidHandler handler = new AndroidHandler(logger.getName());
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            logger.addHandler(handler);
            logger.setLevel(Level.ALL);
        } else {
            logger.setLevel(Level.OFF);
            logger.setFilter((record) -> false);
        }
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

        Context appContext = getApplicationContext();
        mSettings = new DConnectSettings(appContext);
        mPluginManager = new DevicePluginManager(appContext, DConnectConst.LOCALHOST_DCONNECT);
    }

    public DConnectSettings getSettings() {
        return mSettings;
    }

    public DevicePluginManager getPluginManager() {
        return mPluginManager;
    }
}
