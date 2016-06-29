/*
 HitoeApplication
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe;

import android.app.Application;

import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;

/**
 * Implementation of Application.
 * @author NTT DOCOMO, INC.
 */
public class HitoeApplication extends Application {
    /**
     * Instance of HitoeManager.
     */
    private HitoeManager mMgr;

    /**
     * Initialize the HitoeApplication.
     */
    public void initialize() {
        if (mMgr == null) {
            mMgr = new HitoeManager(getApplicationContext());
        }
    }

    /**
     * Gets a instance of HitoeManager.
     * @return HitoeManager
     */
    public HitoeManager getHitoeManager() {
        return mMgr;
    }

}
