/*
HueDeviceApplication
Copyright (c) 2016 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.hue;

import android.app.Application;

import org.deviceconnect.android.deviceplugin.hue.BuildConfig;
import org.deviceconnect.android.logger.AndroidHandler;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Hue Device Application.
 * @author NTT DOCOMO, INC.
 */
public class HueDeviceApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Logger logger = Logger.getLogger("hue.dplugin");
        if (BuildConfig.DEBUG) {
            AndroidHandler handler = new AndroidHandler(logger.getName());
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            logger.addHandler(handler);
            logger.setLevel(Level.ALL);
        } else {
            logger.setLevel(Level.OFF);
        }
    }
}
