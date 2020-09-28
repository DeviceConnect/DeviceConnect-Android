/*
 MidiDemoInstaller.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.demo.DemoInstaller;

/**
 * MIDI デモインストーラ.
 *
 * @author NTT DOCOMO, INC.
 */
public class MidiDemoInstaller extends DemoInstaller {

    public MidiDemoInstaller(Context context) {
        super(context, BuildConfig.PACKAGE_NAME, BuildConfig.DEMO_DIR, BuildConfig.DEMO_ZIP);
    }
}
