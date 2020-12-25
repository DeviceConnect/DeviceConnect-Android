package org.deviceconnect.android.deviceplugin.host.activity.recorder.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.host.activity.fragment.HostDevicePluginBindPreferenceFragment;

public abstract class SettingsBaseFragment extends HostDevicePluginBindPreferenceFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            view.setBackgroundColor(getResources().getColor(android.R.color.white));
        }
        return view;
    }

    /**
     * レコード ID を取得します.
     *
     * @return レコード ID
     */
    public String getRecorderId() {
        Activity activity = getActivity();
        if (activity instanceof SettingsActivity) {
            return ((SettingsActivity) activity).getRecorderId();
        }
        return null;
    }
}
