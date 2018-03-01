package org.deviceconnect.android.manager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
/*
 ThingsSettingActivity.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
import org.deviceconnect.android.manager.setting.SettingActivity;

/**
 * Things用のManagerトップ画面.
 * @author NTT DOCOMO, INC.
 */
public class ThingsSettingActivity extends SettingActivity {
    /**
     * Managerの設定項目.
     */
    private DConnectSettings mSettings;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = ((DConnectApplication) getApplication()).getSettings();
        mSettings.setManagerStartFlag(true);
    }
}
