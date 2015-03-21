/*
 SWApplication.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sw;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.deviceconnect.android.logger.AndroidHandler;
import org.deviceconnect.android.profile.TouchProfile;
import org.deviceconnect.android.profile.KeyEventProfile;

import android.app.Application;
import android.os.Bundle;

/**
 * SonyWatchDevicePlugin_LoggerLevelSetting.
 */
public class SWApplication extends Application {

    /** Cache retention time (mSec). */
    static final long CACHE_RETENTION_TIME = 10000;

    /** Touch profile onTouch cache. */
    static Bundle sOnTouchCache = null;

    /** Touch profile onTouch cache time. */
    static long sOnTouchCacheTime = 0;

    /** Touch profile onTouchStart cache. */
    static Bundle sOnTouchStartCache = null;

    /** Touch profile onTouchStart cache time. */
    static long sOnTouchStartCacheTime = 0;

    /** Touch profile onTouchEnd cache. */
    static Bundle sOnTouchEndCache = null;

    /** Touch profile onTouchEnd cache time. */
    static long sOnTouchEndCacheTime = 0;

    /** Touch profile onDoubleTap cache. */
    static Bundle sOnDoubleTapCache = null;

    /** Touch profile onDoubleTap cache time. */
    static long sOnDoubleTapCacheTime = 0;

    /** Touch profile onTouchMove cache. */
    static Bundle sOnTouchMoveCache = null;

    /** Touch profile onTouchMove cache time. */
    static long sOnTouchMoveCacheTime = 0;

    /** Touch profile onTouchCancel cache. */
    static Bundle sOnTouchCancelCache = null;

    /** Touch profile onTouchCancel cache time. */
    static long sOnTouchCancelCacheTime = 0;

    /**
     * Get Touch cache data.
     * 
     * @param attr Attribute.
     * @return Touch cache data.
     */
    public static Bundle getTouchCache(final String attr) {
        long lCurrentTime = System.currentTimeMillis();
        if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH)) {
            if (lCurrentTime - sOnTouchCacheTime <= CACHE_RETENTION_TIME) {
                return sOnTouchCache;
            } else {
                return null;
            }
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_START)) {
            if (lCurrentTime - sOnTouchStartCacheTime <= CACHE_RETENTION_TIME) {
                return sOnTouchStartCache;
            } else {
                return null;
            }
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_END)) {
            if (lCurrentTime - sOnTouchEndCacheTime <= CACHE_RETENTION_TIME) {
                return sOnTouchEndCache;
            } else {
                return null;
            }
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_DOUBLE_TAP)) {
            if (lCurrentTime - sOnDoubleTapCacheTime <= CACHE_RETENTION_TIME) {
                return sOnDoubleTapCache;
            } else {
                return null;
            }
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_MOVE)) {
            if (lCurrentTime - sOnTouchMoveCacheTime <= CACHE_RETENTION_TIME) {
                return sOnTouchMoveCache;
            } else {
                return null;
            }
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_CANCEL)) {
            if (lCurrentTime - sOnTouchCancelCacheTime <= CACHE_RETENTION_TIME) {
                return sOnTouchCancelCache;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Set Touch data to cache.
     * 
     * @param attr Attribute.
     * @param touchData Touch data.
     */
    public static void setTouchCache(final String attr, final Bundle touchData) {
        long lCurrentTime = System.currentTimeMillis();
        if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH)) {
            sOnTouchCache = touchData;
            sOnTouchCacheTime = lCurrentTime;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_START)) {
            sOnTouchStartCache = touchData;
            sOnTouchStartCacheTime = lCurrentTime;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_END)) {
            sOnTouchEndCache = touchData;
            sOnTouchEndCacheTime = lCurrentTime;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_DOUBLE_TAP)) {
            sOnDoubleTapCache = touchData;
            sOnDoubleTapCacheTime = lCurrentTime;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_MOVE)) {
            sOnTouchMoveCache = touchData;
            sOnTouchMoveCacheTime = lCurrentTime;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_CANCEL)) {
            sOnTouchCancelCache = touchData;
            sOnTouchCancelCacheTime = lCurrentTime;
        }
    }

    /** KeyEvent profile onDown cache. */
    static Bundle sOnDownCache = null;

    /** KeyEvent profile onDown cache time. */
    static long sOnDownCacheTime = 0;

    /** KeyEvent profile onUp cache. */
    static Bundle sOnUpCache = null;

    /** KeyEvent profile onUp cache time. */
    static long sOnUpCacheTime = 0;

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

    /**
     * Device OrientationのデータをサービスIDごとにキャッシュするためのマップ.
     */
    private Map<String, SWDeviceOrientationCache> mCache = 
            new ConcurrentHashMap<String, SWDeviceOrientationCache>();

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

    /**
     * Device Orientationのデータをキャッシュする.
     * @param serviceId Orientationを発行したサービスID
     * @param values Orientationのデータ
     * @param interval Orientationのインターバル
     */
    public void setDeviceOrientationCache(final String serviceId,
            final float[] values, final long interval) {
        SWDeviceOrientationCache cache = mCache.get(serviceId);
        if (cache == null) {
            cache = new SWDeviceOrientationCache();
            mCache.put(serviceId, cache);
        }
        cache.setValues(values);
        cache.setInterval(interval);
    }

    /**
     * Device Orientationデータのキャッシュを取得する.
     * <p>
     * キャッシュがない場合にはnullを返却する.
     * @param serviceId サービスID
     * @return Orientationデータ
     */
    public SWDeviceOrientationCache getDeviceOrientation(final String serviceId) {
        return mCache.get(serviceId);
    }
}
