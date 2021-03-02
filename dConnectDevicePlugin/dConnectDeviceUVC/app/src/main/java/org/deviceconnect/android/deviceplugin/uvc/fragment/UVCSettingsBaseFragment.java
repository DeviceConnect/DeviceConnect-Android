package org.deviceconnect.android.deviceplugin.uvc.fragment;

import android.app.Activity;
import android.content.Intent;
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
        String title = createTitle();
        if (title != null) {
            setTitle(title);
        }
    }

    @Override
    public void onUvcDisconnected(UVCService service) {
        popBackFragment();
    }

    private String createTitle() {
        UVCService service = getUVCService();
        if (service != null) {
            String title = service.getName();
            UvcRecorder recorder = getRecorder();
            if (recorder != null) {
                title += " - " + recorder.getName();
            }
            return title;
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

    public boolean isOnlineUVCService() {
        UVCService service = getUVCService();
        if (service != null) {
            return service.isOnline();
        }
        return false;
    }

    public UvcRecorder getRecorder() {
        UVCService service = getUVCService();
        if (service != null) {
            return service.findUvcRecorderById(getRecorderId());
        }
        return null;
    }

    public String getServiceId() {
        return getArgs("service_id");
    }

    public String getRecorderId() {
        return getArgs("recorder_id");
    }

    public String getSettingsName() {
        return getArgs("settings_name");
    }

    private String getArgs(String key) {
        String serviceId = getBundleArgs(key);
        if (serviceId == null) {
            serviceId = getIntentArgs(key);
        }
        return serviceId;
    }

    private String getBundleArgs(String key) {
        Bundle args = getArguments();
        if (args != null) {
            return args.getString(key);
        }
        return null;
    }

    private String getIntentArgs(String key) {
        Activity a = getActivity();
        if (a != null) {
            Intent args = a.getIntent();
            if (args != null) {
                return args.getStringExtra(key);
            }
        }
        return null;
    }
}
