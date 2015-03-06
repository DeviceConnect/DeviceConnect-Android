/*
 SWApplication.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sw;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.deviceconnect.android.logger.AndroidHandler;
import org.deviceconnect.android.profile.KeyEventProfile;

import android.app.Application;
import android.os.Bundle;

/**
 * SonyWatchDevicePlugin_LoggerLevelSetting.
 */
public class SWApplication extends Application {

    /** KeyEvent profile onDown cache. */
    static Bundle sOnDownCache = null;

    /** KeyEvent profile onUp cache. */
    static Bundle sOnUpCache = null;

    /**
     * Get KeyEvent cache data.
     * 
     * @param attr Attribute.
     * @return KeyEvent cache data.
     */
    public static Bundle getKeyEventCache(final String attr) {
        if (attr.equals(KeyEventProfile.ATTRIBUTE_ON_DOWN)) {
            return sOnDownCache;
        } else if (attr.equals(KeyEventProfile.ATTRIBUTE_ON_UP)) {
            return sOnUpCache;
        } else {
            return null;
        }
    }

    /**
     * Set KeyEvent data to cache.
     * 
     * @param attr Attribute.
     * @param keyeventData Touch data.
     */
    public static void setKeyEventCache(final String attr, final Bundle keyeventData) {
        if (attr.equals(KeyEventProfile.ATTRIBUTE_ON_DOWN)) {
            sOnDownCache = keyeventData;
        } else if (attr.equals(KeyEventProfile.ATTRIBUTE_ON_UP)) {
            sOnUpCache = keyeventData;
        }
    }

    /** ロガー. */
    private Logger mLogger = Logger.getLogger(SWConstants.LOGGER_NAME);

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            AndroidHandler handler = new AndroidHandler(SWConstants.LOGGER_NAME);
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            mLogger.addHandler(handler);
            mLogger.setLevel(Level.ALL);
        } else {
            mLogger.setLevel(Level.OFF);
        }
    }

}
