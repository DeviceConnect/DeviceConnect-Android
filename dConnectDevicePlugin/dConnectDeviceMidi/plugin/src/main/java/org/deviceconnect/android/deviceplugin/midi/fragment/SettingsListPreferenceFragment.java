/*
 SettingsListPreferenceFragment.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.deviceconnect.android.deviceplugin.midi.DConnectMidiServiceListActivity;
import org.deviceconnect.android.deviceplugin.midi.MidiDemoSettingActivity;
import org.deviceconnect.android.deviceplugin.midi.R;

/**
 * Settings List Fragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class SettingsListPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.settings_midi_plugin);
    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        boolean result = super.onPreferenceTreeClick(preference);

        Activity activity = getActivity();
        if (activity == null) {
            return result;
        }
        Context context = activity.getApplicationContext();

        Intent intent = null;
        if (getString(R.string.settings_pref_key_service_list).equals(preference.getKey())) {
            intent = new Intent(context, DConnectMidiServiceListActivity.class);
        } else if (getString(R.string.settings_pref_key_demo_page).equals(preference.getKey())) {
            intent = new Intent(context, MidiDemoSettingActivity.class);
        }

        if (intent != null) {
            activity.startActivity(intent);
        }

        return result;
    }
}
