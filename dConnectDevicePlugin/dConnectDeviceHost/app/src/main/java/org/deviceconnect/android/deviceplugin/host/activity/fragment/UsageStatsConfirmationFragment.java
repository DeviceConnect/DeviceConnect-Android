package org.deviceconnect.android.deviceplugin.host.activity.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.connection.HostConnectionManager;
import org.deviceconnect.android.deviceplugin.host.databinding.FragmentHostUsageStatsConfirmationBinding;

public abstract class UsageStatsConfirmationFragment extends HostDevicePluginBindFragment {
    /**
     * 使用履歴の許可を行う画面の要求を行ったか確認するフラグ.
     */
    private boolean mRequestUsageStats;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentHostUsageStatsConfirmationBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_host_usage_stats_confirmation, container, false);
        binding.setPresenter(this);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        Context context = getContext();
        if (context == null) {
            return;
        }

        if (HostConnectionManager.checkUsageAccessSettings(context)) {
            onNextFragment();
        } else if (mRequestUsageStats) {
            onDeny();
        }
    }

    /**
     * 使用履歴の許可を行う画面を開きます.
     */
    public void onRequestUsageStatusButton() {
        Context context = getContext();
        if (context == null) {
            return;
        }

        if (!HostConnectionManager.checkUsageAccessSettings(context)) {
            mRequestUsageStats = true;
            HostConnectionManager.openUsageAccessSettings(context);
        }
    }

    /**
     * 許可が降りている場合に次の画面に遷移します.
     */
    public abstract void onNextFragment();

    /**
     * 許可が降りなかった場合の処理を行います.
     */
    public abstract void onDeny();
}
