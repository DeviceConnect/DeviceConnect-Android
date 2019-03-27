/*
 HostDemoPageInstaller.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.demo;


import org.deviceconnect.android.deviceplugin.demo.DemoPageInstaller;
import org.deviceconnect.android.deviceplugin.host.BuildConfig;

/**
 * Host プラグインのデモインストーラ.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostDemoPageInstaller extends DemoPageInstaller {

    /**
     * コンストラクタ.
     */
    public HostDemoPageInstaller() {
        super(BuildConfig.PACKAGE_NAME, BuildConfig.DEMO_DIR, BuildConfig.DEMO_ZIP);
    }
}
