package org.deviceconnect.android.deviceplugin.host.activity.recorder.settings;

import android.content.SharedPreferences;

import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

public abstract class SettingsParameterFragment extends SettingsBaseFragment {
    private boolean mChangedValue;
    private final SharedPreferences.OnSharedPreferenceChangeListener mListener =
            (sharedPreferences, key) -> mChangedValue = true;

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(mListener);

        HostMediaRecorder recorder = getRecorder();
        if (mChangedValue && recorder != null) {
            recorder.onConfigChange();
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mChangedValue = false;
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(mListener);
    }
}
