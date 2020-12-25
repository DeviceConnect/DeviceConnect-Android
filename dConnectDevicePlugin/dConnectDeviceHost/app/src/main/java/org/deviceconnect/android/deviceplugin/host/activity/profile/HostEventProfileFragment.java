package org.deviceconnect.android.deviceplugin.host.activity.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.fragment.HostDevicePluginBindFragment;
import org.deviceconnect.android.deviceplugin.host.databinding.FragmentHostEventMainBinding;

public class HostEventProfileFragment extends HostDevicePluginBindFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentHostEventMainBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_host_event_main, container, false);

        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onBindService() {
    }

    @Override
    public void onUnbindService() {
    }
}
