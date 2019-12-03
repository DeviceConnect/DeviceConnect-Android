/*
 ThetaDeviceApplication
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import androidx.collection.LruCache;

import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewApi;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceDetectionFromAccessPoint;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceDetectionFromLAN;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;
import org.deviceconnect.android.deviceplugin.theta.core.sensor.AbstractHeadTracker;
import org.deviceconnect.android.deviceplugin.theta.core.sensor.DefaultHeadTracker;
import org.deviceconnect.android.deviceplugin.theta.core.sensor.HeadTracker;
import org.deviceconnect.android.deviceplugin.theta.core.sensor.HeadTrackingListener;
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

    private final Logger[] mLoggers = {
        Logger.getLogger("theta.dplugin"),
        Logger.getLogger("theta.sampleapp")
    };

    private ThetaDeviceManager mDeviceMgr;

    private HeadTracker mHeadTracker;

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
            for (Logger logger : mLoggers) {
                AndroidHandler handler = new AndroidHandler(logger.getName());
                handler.setFormatter(new SimpleFormatter());
                handler.setLevel(Level.ALL);
                logger.addHandler(handler);
                logger.setLevel(Level.ALL);
            }
        } else {
            for (Logger logger : mLoggers) {
                logger.setLevel(Level.OFF);
            }
        }

        Context context = getApplicationContext();
        mDeviceMgr = new ThetaDeviceManager(context);
        mDeviceMgr.addDeviceDetection(new ThetaDeviceDetectionFromAccessPoint());
        // 16以上でNsdManagerが使用できるので追加する
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mDeviceMgr.addDeviceDetection(new ThetaDeviceDetectionFromLAN());
        }
        mHeadTracker = new HeadTrackerWrapper(new DefaultHeadTracker(context));
        mSphericalViewApi = new SphericalViewApi(context);
    }

    public ThetaDeviceManager getDeviceManager() {
        return mDeviceMgr;
    }

    public HeadTracker getHeadTracker() {
        return mHeadTracker;
    }

    public SphericalViewApi getSphericalViewApi() {
        return mSphericalViewApi;
    }

    public LruCache<String, byte[]> getCache() {
        return mThumbnailCache;
    }

    private static class HeadTrackerWrapper implements HeadTracker {

        private final AbstractHeadTracker mHeadTracker;

        public HeadTrackerWrapper(final AbstractHeadTracker tracker) {
            mHeadTracker = tracker;
        }

        @Override
        public void start() {
            mHeadTracker.start();
        }

        @Override
        public void stop() {
            mHeadTracker.stop();
        }

        @Override
        public void reset() {
            mHeadTracker.reset();
        }

        @Override
        public synchronized void registerTrackingListener(final HeadTrackingListener listener) {
            if (mHeadTracker.getListenerCount() == 0) {
                mHeadTracker.start();
            }
            mHeadTracker.registerTrackingListener(listener);
        }

        @Override
        public synchronized void unregisterTrackingListener(final HeadTrackingListener listener) {
            mHeadTracker.unregisterTrackingListener(listener);
            if (mHeadTracker.getListenerCount() == 0) {
                mHeadTracker.stop();
            }
        }

    }
}
