/*
 HostSettingActivity.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.setting;

import android.os.Bundle;
import androidx.appcompat.app.ActionBar;

import org.deviceconnect.android.deviceplugin.host.R;

/**
 * Host プラグインの設定画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostSettingActivity extends BaseHostSettingActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.host_settings_title);
        }
    }
}
