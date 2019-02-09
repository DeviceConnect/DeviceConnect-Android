/*
 HostSettingFragment.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.setting;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import org.deviceconnect.android.deviceplugin.host.R;

/**
 * Host プラグインの設定全体を管理するフラグメント.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostSettingFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_host_plugin);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        boolean result = super.onPreferenceTreeClick(preference);

        // 各説明をダイアログで表示
        Fragment fragment = null;
        if (getString(R.string.pref_key_settings_gps).equals(preference.getKey())) {
            fragment = new HostGpsSettingFragment();
        } else if (getString(R.string.pref_key_settings_jpeg_quality_preview).equals(preference.getKey())) {
            fragment = new HostRecorderSettingFragment();
        } else if (getString(R.string.pref_key_settings_demo_page).equals(preference.getKey())) {
            fragment = new HostDemoPageSettingFragment();
        }
        if (fragment != null) {
            FragmentTransaction transaction = getFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.add(android.R.id.content, fragment);
            transaction.addToBackStack("settings");
            transaction.commit();
        }

        return result;
    }


}
