package org.deviceconnect.android.deviceplugin.host.activity.recorder.settings;

import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.host.R;

public class SettingsAudioFragment extends SettingsBaseFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(getRecorderId());
        setPreferencesFromResource(R.xml.settings_host_recorder_audio, rootKey);
    }

    @Override
    public void onBindService() {
        setInputTypeNumber("preview_audio_bitrate");
        setInputTypeNumber("preview_audio_channel");
    }
}