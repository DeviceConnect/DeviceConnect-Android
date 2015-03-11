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

    /** KeyEvent profile onDown cache time. */
    static long sOnDownCacheTime = 0;

    /** KeyEvent profile onUp cache. */
    static Bundle sOnUpCache = null;

    /** KeyEvent profile onUp cache time. */
    static long sOnUpCacheTime = 0;

    /** KeyEvent profile cache retention time (mSec). */
    static final long CACHE_RETENTION_TIME = 10000;

    /**
     * Get KeyEvent cache data.
     * 
     * @param attr Attribute.
     * @return KeyEvent cache data.
     */
    public static Bundle getKeyEventCache(final String attr) {
        long lCurrentTime = System.currentTimeMillis();
        if (attr.equals(KeyEventProfile.ATTRIBUTE_ON_DOWN)) {
            if (lCurrentTime - sOnDownCacheTime <= CACHE_RETENTION_TIME) {
                return sOnDownCache;
            } else {
                return null;
            }
        } else if (attr.equals(KeyEventProfile.ATTRIBUTE_ON_UP)) {
            if (lCurrentTime - sOnUpCacheTime <= CACHE_RETENTION_TIME) {
                return sOnUpCache;
            } else {
                return null;
            }
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
        long lCurrentTime = System.currentTimeMillis();
        if (attr.equals(KeyEventProfile.ATTRIBUTE_ON_DOWN)) {
            sOnDownCache = keyeventData;
            sOnDownCacheTime = lCurrentTime;
        } else if (attr.equals(KeyEventProfile.ATTRIBUTE_ON_UP)) {
            sOnUpCache = keyeventData;
            sOnUpCacheTime = lCurrentTime;
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
