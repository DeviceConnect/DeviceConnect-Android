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

import android.app.Application;

/**
 * SonyWatchDevicePlugin_LoggerLevelSetting.
 */
public class SWApplication extends Application {

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
