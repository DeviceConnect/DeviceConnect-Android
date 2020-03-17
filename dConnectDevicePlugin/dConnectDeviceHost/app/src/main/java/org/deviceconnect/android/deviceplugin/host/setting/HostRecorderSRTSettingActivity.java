package org.deviceconnect.android.deviceplugin.host.setting;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;

import org.deviceconnect.android.deviceplugin.host.R;

/**
 * SRT ソケットのオプション設定を行うための Activity.
 */
public class HostRecorderSRTSettingActivity extends BaseHostSettingActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_settings_srt);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.host_settings_item_srt_preview));
        }
    }
}
