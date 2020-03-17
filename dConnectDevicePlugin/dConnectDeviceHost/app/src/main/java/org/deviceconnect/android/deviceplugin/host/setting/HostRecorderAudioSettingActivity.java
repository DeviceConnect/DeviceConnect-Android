package org.deviceconnect.android.deviceplugin.host.setting;

import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.host.R;

import androidx.appcompat.app.ActionBar;

/**
 * プレビューの音声設定を行うための Activity.
 */
public class HostRecorderAudioSettingActivity extends BaseHostSettingActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_settings_audio);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.host_settings_item_audio_preview));
        }
    }
}
