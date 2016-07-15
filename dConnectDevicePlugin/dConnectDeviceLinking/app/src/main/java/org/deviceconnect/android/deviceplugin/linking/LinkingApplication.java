/*
 LinkingApplication.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking;

import android.app.Application;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
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

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingApplication#onCreate");
        }

        mBeaconManager = new LinkingBeaconManager(this);
        mDeviceManager = new LinkingDeviceManager(this);
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
}
