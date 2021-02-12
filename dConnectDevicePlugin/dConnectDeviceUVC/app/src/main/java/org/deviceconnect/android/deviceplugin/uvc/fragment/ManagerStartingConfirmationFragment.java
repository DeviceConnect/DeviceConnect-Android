package org.deviceconnect.android.deviceplugin.uvc.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.deviceconnect.android.deviceplugin.uvc.R;
import org.deviceconnect.android.deviceplugin.uvc.activity.UVCDevicePluginBindActivity;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

public class ManagerStartingConfirmationFragment extends UVCDevicePluginBindFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_uvc_manager_starting_confirmation, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
//        startManager();
        onNextFragment();
    }

    @Override
    public void onBindService() {
        Log.e("ABC", "######## AAAAAA onBindService");

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
    public void onNextFragment() {
        findNavController(ManagerStartingConfirmationFragment.this).navigate(R.id.action_check_to_permission);
    }

    /**
     * Device Connect Manager の起動を行います.
     */
    private void startManager() {
        UVCDevicePluginBindActivity a = (UVCDevicePluginBindActivity) getActivity();
        if (a != null && !a.isManagerStarted()) {
            a.startManager();
        }
    }
}
