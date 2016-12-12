package org.deviceconnect.android.app.simplebot;

import android.app.Application;

import org.deviceconnect.android.app.simplebot.utils.DConnectHelper;

public class SimpleBotApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DConnectHelper.INSTANCE.setContext(this);
    }
}
