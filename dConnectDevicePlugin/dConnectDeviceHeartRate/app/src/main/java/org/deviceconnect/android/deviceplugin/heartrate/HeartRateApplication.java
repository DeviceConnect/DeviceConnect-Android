/*
 HeartRateApplication
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate;

import android.app.Application;

import org.deviceconnect.android.deviceplugin.heartrate.ble.BleUtils;

/**
 * @author NTT DOCOMO, INC.
 */
public class HeartRateApplication extends Application {
    /**
     * Instance of HeartRateManager.
     */
    private HeartRateManager mMgr;

    /**
     * Initialize the HeartRateApplication.
     */
    public void initialize() {
        if (mMgr == null && BleUtils.isBLESupported(getApplicationContext())) {
            mMgr = new HeartRateManager(getApplicationContext());
        }
    }

    /**
     * Gets a instance of HeartRateManager.
     * @return HeartRateManager
     */
    public HeartRateManager getHeartRateManager() {
        return mMgr;
    }
}
