/*
 HostRecorderSettingActivity.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.setting;

import android.os.Bundle;
import androidx.annotation.Nullable;

import org.deviceconnect.android.deviceplugin.host.R;

/**
 * レコーダー設定画面.
 *
 * 各レコーダーの JPEG 品質を設定するための UI を提供する.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostRecorderSettingActivity extends BaseHostSettingActivity {

    @Override
    protected void onCreate(final @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_settings_recorder);
    }
}
