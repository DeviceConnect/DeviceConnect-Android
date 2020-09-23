/*
 MidiPluginApplication.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi;


import android.app.Application;

import org.deviceconnect.android.logger.AndroidHandler;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * MIDI プラグインアプリケーション.
 * @author NTT DOCOMO, INC.
 */
public class MidiPluginApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        setupLogger("midi-plugin");
    }

    private void setupLogger(final String name) {
        Logger logger = Logger.getLogger(name);
        if (BuildConfig.DEBUG) {
            AndroidHandler handler = new AndroidHandler(logger.getName());
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            logger.addHandler(handler);
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
        } else {
            logger.setLevel(Level.OFF);
            logger.setFilter((record) -> false);
        }
    }
}
