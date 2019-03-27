/*
 HostDemoInstaller.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.demo;


import android.content.Context;

import org.deviceconnect.android.deviceplugin.demo.DemoInstaller;
import org.deviceconnect.android.deviceplugin.host.BuildConfig;

/**
 * Host プラグインのデモインストーラ.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostDemoInstaller extends DemoInstaller {

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public HostDemoInstaller(final Context context) {
        super(context, BuildConfig.PACKAGE_NAME, BuildConfig.DEMO_DIR, BuildConfig.DEMO_ZIP);
    }
}
