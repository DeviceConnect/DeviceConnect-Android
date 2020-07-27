package org.deviceconnect.android.deviceplugin.midi;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.demo.DemoInstaller;

class MidiDemoInstaller extends DemoInstaller {

    public MidiDemoInstaller(Context context) {
        super(context, BuildConfig.PACKAGE_NAME, BuildConfig.DEMO_DIR, BuildConfig.DEMO_ZIP);
    }
}
