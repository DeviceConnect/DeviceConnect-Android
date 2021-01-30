package org.deviceconnect.android.deviceplugin.host.activity.recorder.settings;

import android.os.Bundle;

import androidx.preference.Preference;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

public class SettingsAudioFragment extends SettingsParameterFragment {
    private HostMediaRecorder mMediaRecorder;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(getRecorderId());
        setPreferencesFromResource(R.xml.settings_host_recorder_audio, rootKey);
    }

    @Override
    public void onBindService() {
        mMediaRecorder = getRecorder();

        setAudioEnabled();
        setInputTypeNumber("preview_audio_bitrate");
        setInputTypeNumber("preview_audio_channel");
    }

    private void setAudioEnabled() {
        Preference pref = findPreference("audio_enabled");
        if (pref != null) {
            pref.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
        }
    }

    private final Preference.OnPreferenceChangeListener mOnPreferenceChangeListener = (preference, newValue) -> {
        String key = preference.getKey();
        if ("preview_audio_source".equals(key)) {
            mMediaRecorder.getSettings().setMute(!"none".equalsIgnoreCase((String) newValue));
        }
        return true;
    };
}