package org.deviceconnect.android.deviceplugin.switchbot.demo;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.demo.DemoInstaller;
import org.deviceconnect.android.deviceplugin.switchbot.BuildConfig;

public class SwitchBotDemoInstaller extends DemoInstaller {
    public SwitchBotDemoInstaller(final Context context) {
        super(context, BuildConfig.PACKAGE_NAME, BuildConfig.DEMO_DIR, BuildConfig.DEMO_ZIP);
    }
}
