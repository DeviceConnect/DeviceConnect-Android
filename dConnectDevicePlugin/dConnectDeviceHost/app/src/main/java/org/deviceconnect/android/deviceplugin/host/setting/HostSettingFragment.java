/*
 HostSettingFragment.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.setting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.deviceconnect.android.deviceplugin.host.R;

/**
 * Host プラグインの設定全体を管理するフラグメント.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostSettingFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreate(final @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_host_plugin);
    }

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        boolean result = super.onPreferenceTreeClick(preference);

        Activity activity = getActivity();
        if (activity == null) {
            return result;
        }
        Context context = activity.getApplicationContext();

        // 各説明をダイアログで表示
        Intent intent = null;
        if (getString(R.string.pref_key_settings_gps).equals(preference.getKey())) {
            intent = new Intent(context, HostGpsSettingActivity.class);
        } else if (getString(R.string.pref_key_settings_jpeg_quality_preview).equals(preference.getKey())) {
            intent = new Intent(context, HostRecorderSettingActivity.class);
        } else if (getString(R.string.pref_key_settings_demo_page).equals(preference.getKey())) {
            intent = new Intent(context, HostDemoSettingActivity.class);
        }
        if (intent != null) {
            activity.startActivity(intent);
        }

        return result;
    }


}
