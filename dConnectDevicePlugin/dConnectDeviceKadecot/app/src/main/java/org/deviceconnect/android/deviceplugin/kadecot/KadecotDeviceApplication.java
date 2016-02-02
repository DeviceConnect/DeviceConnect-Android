/*
 KadecotDeviceApplication
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot;

import android.app.Application;

import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.ENLObject;
import org.deviceconnect.android.logger.AndroidHandler;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Kadecot Device Application.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotDeviceApplication extends Application {

    /** Logger. */
    private Logger mLogger = Logger.getLogger("kadecot.dplugin");
    /** ECHONET Lite Object. */
    private ENLObject mObject;

    @Override
    public void onCreate() {
        super.onCreate();

        mObject = new ENLObject();

        if (BuildConfig.DEBUG) {
            AndroidHandler handler = new AndroidHandler("kadecot.dplugin");
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            mLogger.addHandler(handler);
            mLogger.setLevel(Level.ALL);
        } else {
            mLogger.setLevel(Level.OFF);
        }
    }

    /**
     * Get ECHONET Lite Object.
     *
     * @return ECHONET Lite object.
     */
    public ENLObject getENLObject() {
        return mObject;
    }

}
