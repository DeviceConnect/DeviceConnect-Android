/*
 DConnectMidiSettingsListActivity.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

/**
 * MIDI プラグイン設定リスト画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectMidiSettingsListActivity extends FragmentActivity {

    @Override
    protected void onCreate(final @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_list);
    }
}
