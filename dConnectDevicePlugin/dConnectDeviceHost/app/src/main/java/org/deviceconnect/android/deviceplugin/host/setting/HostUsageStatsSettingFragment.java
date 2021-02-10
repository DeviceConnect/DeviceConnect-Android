package org.deviceconnect.android.deviceplugin.host.setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.connection.HostConnectionManager;

public class HostUsageStatsSettingFragment extends BaseHostSettingPageFragment {
    @Override
    protected String getPageTitle() {
        return getString(R.string.usage_stats_settings_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_host_usage_stats_setting, null);
        rootView.findViewById(R.id.btn_settings_open_usage_stats).setOnClickListener((v) ->
                HostConnectionManager.openUsageAccessSettings(getContext()));
        return rootView;
    }
}
