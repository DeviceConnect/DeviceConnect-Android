/*
 UVCDeviceApplication.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc;

import android.app.Application;

import org.deviceconnect.android.deviceplugin.uvc.core.UVCDeviceManager;
import org.deviceconnect.android.logger.AndroidHandler;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * UVC Device Application.
 *
 * @author NTT DOCOMO, INC.
 */
public class UVCDeviceApplication extends Application {

    private final Logger mLogger = Logger.getLogger("uvc.dplugin");

    private UVCDeviceManager mDeviceMgr;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            AndroidHandler handler = new AndroidHandler("uvc.dplugin");
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            mLogger.addHandler(handler);
            mLogger.setLevel(Level.ALL);
        } else {
            mLogger.setLevel(Level.OFF);
        }

        mDeviceMgr = new UVCDeviceManager(getApplicationContext());
    }

    public UVCDeviceManager getDeviceManager() {
        return mDeviceMgr;
    }

}
