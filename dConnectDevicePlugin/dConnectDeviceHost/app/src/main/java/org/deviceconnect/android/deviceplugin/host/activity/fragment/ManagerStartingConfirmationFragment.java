package org.deviceconnect.android.deviceplugin.host.activity.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.HostDevicePluginBindActivity;
import org.deviceconnect.android.libmedia.streaming.util.PermissionUtil;

import java.util.List;

public abstract class ManagerStartingConfirmationFragment extends HostDevicePluginBindFragment {
    /**
     * パーミッションのリクエストコード.
     */
    private static final int PERMISSION_REQUEST_CODE = 12345;

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            List<String> denies = PermissionUtil.checkRequestPermissionsResult(permissions, grantResults);
            if (!denies.isEmpty()) {
                onNextFragment();
            } else {
                onPermissionDeny();
            }
        }
    }

    @Override
    public void onBindService() {
        List<String> denies =  PermissionUtil.checkPermissions(getContext(), getPermissions());
        if (!denies.isEmpty()) {
            requestPermissions(denies.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            onNextFragment();
        }
    }

    @Override
    public void onUnbindService() {
    }

    public abstract String[] getPermissions();

    public abstract void onNextFragment();
    public abstract void onPermissionDeny();

    private void startManager() {
        HostDevicePluginBindActivity a = (HostDevicePluginBindActivity) getActivity();
        if (a != null && !a.isManagerStarted()) {
            a.startManager();
        }
    }
}
