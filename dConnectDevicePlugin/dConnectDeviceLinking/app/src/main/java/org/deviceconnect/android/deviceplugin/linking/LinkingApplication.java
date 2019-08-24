/*
 LinkingApplication.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.lib.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.linking.ConfirmActivity;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.event.EventManager;

/**
 * Implementation of Application.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingApplication extends Application {

    private static final String TAG = "LinkingApplication";

    private LinkingBeaconManager mBeaconManager;
    private LinkingDeviceManager mDeviceManager;

    private int mResumedCount;
    private int mPausedCount;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingApplication#onCreate");
        }

        mBeaconManager = new LinkingBeaconManager(this);
        mDeviceManager = new LinkingDeviceManager(this);

        registerActivityLifecycleCallbacks(new MyLifecycleHandler());
    }

    @Override
    public void onTerminate() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingApplication#onTerminate");
        }

        if (mBeaconManager != null) {
            mBeaconManager.destroy();
            mBeaconManager = null;
        }

        if (mDeviceManager != null) {
            mDeviceManager.destroy();
            mDeviceManager = null;
        }

        super.onTerminate();
    }

    public void resetManager() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingApplication#resetManager");
        }

        if (mBeaconManager != null) {
            mBeaconManager.destroy();
            mBeaconManager = null;
        }
        if (mDeviceManager != null) {
            mDeviceManager.destroy();
            mDeviceManager = null;
        }
        mBeaconManager = new LinkingBeaconManager(this);
        mDeviceManager = new LinkingDeviceManager(this);

        EventManager.INSTANCE.removeAll();
    }

    public LinkingBeaconManager getLinkingBeaconManager() {
        return mBeaconManager;
    }

    public LinkingDeviceManager getLinkingDeviceManager() {
        return mDeviceManager;
    }

    public boolean isStartedConfirmActivity() {
        return (mResumedCount > mPausedCount);
    }

    private final class MyLifecycleHandler implements ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) {
        }

        @Override
        public void onActivityDestroyed(final Activity activity) {
        }

        @Override
        public void onActivityResumed(final Activity activity) {
            if (activity instanceof ConfirmActivity) {
                ++mResumedCount;
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onActivityResumed: ");
                }
            }
        }

        @Override
        public void onActivityPaused(final Activity activity) {
            if (activity instanceof ConfirmActivity) {
                ++mPausedCount;
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onActivityPaused: ");
                }
            }
        }

        @Override
        public void onActivitySaveInstanceState(final Activity activity, final Bundle outState) {
        }

        @Override
        public void onActivityStarted(final Activity activity) {
        }

        @Override
        public void onActivityStopped(final Activity activity) {
        }
    }
}
