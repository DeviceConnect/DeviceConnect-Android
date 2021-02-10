/*
 HostSettingActivity.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.setting;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;

import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.HostDevicePluginBindActivity;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorderManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Host プラグインの設定画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostSettingActivity extends HostDevicePluginBindActivity {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.host_settings_title);
        }
    }

    public List<HostMediaRecorder> getRecorderList() {
        HostDevicePlugin plugin = getHostDevicePlugin();
        if (plugin == null) {
            return new ArrayList<>();
        }
        HostMediaRecorderManager mgr = plugin.getHostMediaRecorderManager();
        return Arrays.asList(mgr.getRecorders());
    }
}
