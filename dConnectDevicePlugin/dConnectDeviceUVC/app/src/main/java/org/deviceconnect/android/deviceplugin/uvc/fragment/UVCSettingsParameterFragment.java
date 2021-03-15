package org.deviceconnect.android.deviceplugin.uvc.fragment;

import android.content.SharedPreferences;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;

import org.deviceconnect.android.deviceplugin.uvc.fragment.preference.SeekBarDialogFragment;
import org.deviceconnect.android.deviceplugin.uvc.fragment.preference.SeekBarDialogPreference;
import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;

public abstract class UVCSettingsParameterFragment extends UVCSettingsBaseFragment {
    /**
     * カスタム Preference を判別するためのタグ.
     */
    private static final String DIALOG_FRAGMENT_TAG = "CustomPreference";

    /**
     * 設定が変更されたことを保持するフラグ.
     */
    private boolean mChangedValue;

    /**
     * 設定が変更されてことを受信するリスナー.
     */
    private final SharedPreferences.OnSharedPreferenceChangeListener mListener =
            (sharedPreferences, key) -> mChangedValue = true;

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return;
        }

        if (preference instanceof SeekBarDialogPreference) {
            DialogFragment f = SeekBarDialogFragment.newInstance(preference.getKey());
            f.setTargetFragment(this, 0);
            f.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(mListener);

        MediaRecorder recorder = getRecorder();

        // 設定が変更されていた場合には、レコーダに通知を行う
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
