package org.deviceconnect.android.deviceplugin.uvc.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceService;
import org.deviceconnect.android.deviceplugin.uvc.activity.UVCDevicePluginBindActivity;
import org.deviceconnect.android.deviceplugin.uvc.activity.UVCSettingsActivity;
import org.deviceconnect.android.deviceplugin.uvc.recorder.uvc.UvcRecorder;
import org.deviceconnect.android.deviceplugin.uvc.service.UVCService;

public abstract class UVCSettingsBaseFragment extends UVCDevicePluginBindPreferenceFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            view.setBackgroundColor(getResources().getColor(android.R.color.white));
        }
        return view;
    }

    @Override
    public void onBindService() {
        UVCService service = getUVCService();
        if (service != null) {
            setTitle(service.getName());
        }
    }

    public UVCDeviceService getUVCDeviceService() {
        Activity activity = getActivity();
        if (activity instanceof UVCDevicePluginBindActivity) {
            return ((UVCDevicePluginBindActivity) activity).getUVCDeviceService();
        }
        return null;
    }

    public UVCService getUVCService() {
        UVCDeviceService deviceService = getUVCDeviceService();
        if (deviceService != null) {
            return deviceService.findUVCServiceById(getServiceId());
        }
        return null;
    }

    public UvcRecorder getRecorder() {
        UVCService service = getUVCService();
        if (service != null) {
            return  service.findUvcRecorderById(getRecorderId());
        }
        return null;
    }

    public String getServiceId() {
        Bundle args = getArguments();
        if (args != null) {
            return args.getString("service_id");
        }
        return null;
    }

    public String getRecorderId() {
        Bundle args = getArguments();
        if (args != null) {
            return args.getString("recorder_id");
        }
        return null;
    }

    public String getSettingsName() {
        Bundle args = getArguments();
        if (args != null) {
            return args.getString("settings_name");
        }
        return null;
    }
}
