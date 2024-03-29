package org.deviceconnect.android.deviceplugin.host.activity.recorder.settings;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.util.NetworkUtil;

public class SettingsSRTFragment extends SettingsEncoderFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(getEncoderId());
        setPreferencesFromResource(R.xml.settings_host_recorder_srt, rootKey);

        setSummaryOptionAuto(getString(R.string.pref_key_settings_srt_inputbw));
        setSummaryOptionAuto(getString(R.string.pref_key_settings_srt_oheadbw));

        setInputTypeNumber(getString(R.string.pref_key_settings_srt_peerlatency));
        setInputTypeNumber(getString(R.string.pref_key_settings_srt_lossmaxttl));
        setInputTypeNumber(getString(R.string.pref_key_settings_srt_oheadbw));
        setInputTypeNumber(getString(R.string.pref_key_settings_srt_inputbw));
        setInputTypeNumber(getString(R.string.pref_key_settings_srt_conntimeo));
        setInputTypeNumber(getString(R.string.pref_key_settings_srt_peeridletimeo));
        setInputTypeNumber(getString(R.string.pref_key_settings_srt_packetfilter));
    }

    @Override
    protected String getServerUrl(int port) {
        String ipAddress = NetworkUtil.getIPAddress(requireContext());
        return "srt://" + ipAddress + ":" + port;
    }

    private void setSummaryOptionAuto(String name) {
        EditTextPreference inputBwPref = findPreference(name);
        if (inputBwPref != null) {
            inputBwPref.setSummaryProvider(summaryOptionAuto);
        }
    }

    private final Preference.SummaryProvider<EditTextPreference> summaryOptionAuto = (preference) -> {
        String value = preference.getText();
        if (TextUtils.isEmpty(value)) {
            return getString(R.string.host_setting_srt_option_auto);
        }
        return value;
    };
}