/*
 HeartRateApplication
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate;

import android.app.Application;

import org.deviceconnect.android.deviceplugin.heartrate.ble.BleUtils;
import org.deviceconnect.android.logger.AndroidHandler;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of Application.
 * @author NTT DOCOMO, INC.
 */
public class HeartRateApplication extends Application {
    /**
     * Instance of HeartRateManager.
     */
    private HeartRateManager mMgr;

    /** Logger. */
    private final Logger mLogger = Logger.getLogger("heartrate.dplugin");

    @Override
    public void onCreate() {
        super.onCreate();

        if (!BuildConfig.DEBUG) {
            mLogger.setLevel(Level.OFF);
        }
    }

    /**
     * Initialize the HeartRateApplication.
     */
    public void initialize() {
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

        if (mMgr == null && BleUtils.isBLESupported(getApplicationContext())) {
            mMgr = new HeartRateManager(getApplicationContext());
        }
    }

    /**
     * Gets a instance of HeartRateManager.
     * @return HeartRateManager
     */
    public HeartRateManager getHeartRateManager() {
        return mMgr;
    }
}
