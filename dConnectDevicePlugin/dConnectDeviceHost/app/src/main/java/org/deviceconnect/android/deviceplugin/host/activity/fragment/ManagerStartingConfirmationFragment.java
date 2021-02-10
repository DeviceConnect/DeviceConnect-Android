package org.deviceconnect.android.deviceplugin.host.activity.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.HostDevicePluginBindActivity;

public abstract class ManagerStartingConfirmationFragment extends HostDevicePluginBindFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_host_manager_starting_confirmation, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        startManager();
    }

    @Override
    public void onBindService() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        onNextFragment();
    }

    @Override
    public void onUnbindService() {
    }

    /**
     * Device Connect Manager に接続できた場合に次の画面に遷移します.
     */
    public abstract void onNextFragment();

    /**
     * Device Connect Manager の起動を行います.
     */
    private void startManager() {
        HostDevicePluginBindActivity a = (HostDevicePluginBindActivity) getActivity();
        if (a != null && !a.isManagerStarted()) {
            a.startManager();
        }
    }
}
