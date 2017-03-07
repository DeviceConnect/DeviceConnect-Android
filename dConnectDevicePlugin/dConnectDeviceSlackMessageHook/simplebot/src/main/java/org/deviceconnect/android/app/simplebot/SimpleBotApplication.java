/*
 SimpleBotService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.app.simplebot;

import android.app.Application;

import org.deviceconnect.android.app.simplebot.utils.DConnectHelper;

public class SimpleBotApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DConnectHelper.INSTANCE.setContext(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        DConnectHelper.INSTANCE.setContext(null);
    }
}
