/*
 ThetaDeviceApplication
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta;

import android.app.Application;
import android.support.v4.util.LruCache;

import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewApi;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;
import org.deviceconnect.android.logger.AndroidHandler;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Theta Device Application.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaDeviceApplication extends Application {

    private Logger mLogger = Logger.getLogger("theta.dplugin");

    private ThetaDeviceManager mDeviceMgr;

    private SphericalViewApi mSphericalViewApi;
    /**
     * Cache size of thumbnail.
     *
     * 3 Thumbnails will be cached.
     *
     * The size per thumbnail is about 3 KBytes.
     *
     * Unit: byte.
     */
    private static final int THUMBNAIL_CACHE_SIZE = (2 * 1024 * 1024) * 3;
    private LruCache<String, byte[]> mThumbnailCache = new LruCache<String, byte[]>(THUMBNAIL_CACHE_SIZE) {
        @Override
        protected int sizeOf(final String key, final byte[] value) {
            return value.length / 1024;
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            AndroidHandler handler = new AndroidHandler("theta.dplugin");
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            mLogger.addHandler(handler);
            mLogger.setLevel(Level.ALL);
        } else {
            mLogger.setLevel(Level.OFF);
        }

        mDeviceMgr = new ThetaDeviceManager(getApplicationContext());
        mDeviceMgr.checkConnectedDevice();
        mSphericalViewApi = new SphericalViewApi(getApplicationContext());
    }

    public ThetaDeviceManager getDeviceManager() {
        return mDeviceMgr;
    }

    public SphericalViewApi getSphericalViewApi() {
        return mSphericalViewApi;
    }
    public LruCache<String, byte[]> getCache() {
        return mThumbnailCache;
    }
}
