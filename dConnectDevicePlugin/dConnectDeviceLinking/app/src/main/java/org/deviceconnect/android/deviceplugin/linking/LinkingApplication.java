/*
 LinkingApplication.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking;

import android.app.Application;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManagerMockImpl;

/**
 * Implementation of Application.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingApplication extends Application {

    private static final String TAG = "LinkingApplication";
    private LinkingManager mManager;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onCreate");
        }
        mManager = new LinkingManagerMockImpl();
    }

    @Override
    public void onTerminate() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onTerminate");
        }
        super.onTerminate();
    }

}
