package org.deviceconnect.android.deviceplugin.host.activity.recorder.settings;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.fragment.SeekBarDialogPreference;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

import java.util.ArrayList;
import java.util.List;

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
        if (mMediaRecorder == null) {
            return;
        }

        setPreviewAudioSource(mMediaRecorder.getSettings());
        setPreviewSampleRate(mMediaRecorder.getSettings());
        setPreviewAudioCoefficient(mMediaRecorder.getSettings());
        setInputTypeNumber("preview_audio_bitrate");
    }

    /**
     * 静止画の解像度の Preference に値を設定します.
     *
     * @param settings レコーダの設定
     */
    private void setPreviewAudioSource(HostMediaRecorder.Settings settings) {
        ListPreference pref = findPreference("preview_audio_source");
        if (pref != null) {
            List<HostMediaRecorder.AudioSource> list = settings.getSupportedAudioSource();
            if (list != null && !list.isEmpty()) {
                List<String> entryNames = new ArrayList<>();
                List<String> entryValues = new ArrayList<>();
                entryNames.add("None");
                entryValues.add("none");
                for (HostMediaRecorder.AudioSource audioSource : list) {
                    switch (audioSource) {
                        case APP:
                            entryNames.add(getString(R.string.host_recorder_settings_audio_source_app));
                            entryValues.add("app");
                            break;
                        case MIC:
                            entryNames.add(getString(R.string.host_recorder_settings_audio_source_mic));
                            entryValues.add("mic");
                            break;
                        case DEFAULT:
                            entryNames.add(getString(R.string.host_recorder_settings_audio_source_default));
                            entryValues.add("default");
                            break;
                    }
                }
                pref.setEntries(entryNames.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
            } else {
                pref.setEnabled(false);
            }
        }
    }

    /**
     * サンプルレートの Preference に値を設定します.
     *
     * @param settings レコーダの設定
     */
    private void setPreviewSampleRate(HostMediaRecorder.Settings settings) {
        ListPreference pref = findPreference("preview_audio_sample_rate");
        if (pref != null) {
            List<Integer> list = settings.getSupportedSampleRateList();
            if (list != null && !list.isEmpty()) {
                List<String> entryNames = new ArrayList<>();
                List<String> entryValues = new ArrayList<>();
                for (Integer sampleRate : list) {
                    entryNames.add(String.valueOf(sampleRate));
                    entryValues.add(String.valueOf(sampleRate));
                }
                pref.setEntries(entryNames.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
            } else {
                pref.setEnabled(false);
            }
        }
    }

    /**
     * フィルターの係数用の Preference に値を設定します.
     *
     * @param settings レコーダ設定
     */
    private void setPreviewAudioCoefficient(HostMediaRecorder.Settings settings) {
        SeekBarDialogPreference pref = findPreference("preview_audio_coefficient");
        if (pref != null) {
            pref.setMinValue(0);
            pref.setMaxValue(100);
        }
    }

    private final Preference.OnPreferenceChangeListener mOnPreferenceChangeListener = (preference, newValue) -> {
        return mMediaRecorder != null;
    };
}