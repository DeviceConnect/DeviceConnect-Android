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

/**
 * Implementation of Application.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingApplication extends Application {

    private static final String TAG = "LinkingApplication";

    private LinkingBeaconManager mBeaconManager;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onCreate");
        }

        mBeaconManager = new LinkingBeaconManager(this);
    }

    @Override
    public void onTerminate() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onTerminate");
        }

        if (mBeaconManager != null) {
            mBeaconManager.destroy();
            mBeaconManager = null;
        }

        super.onTerminate();
    }

    public LinkingBeaconManager getLinkingBeaconManager() {
        return mBeaconManager;
    }
}
