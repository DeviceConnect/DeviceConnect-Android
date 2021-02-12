package org.deviceconnect.android.deviceplugin.uvc.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import org.deviceconnect.android.deviceplugin.uvc.R;
import org.deviceconnect.android.deviceplugin.uvc.databinding.FragmentUvcCameraMainBinding;
import org.deviceconnect.android.deviceplugin.uvc.recorder.h264.UvcH264Recorder;

public class UVCCameraMainFragment extends UVCDevicePluginBindFragment {

    private UvcH264Recorder mUvcRecorder;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentUvcCameraMainBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_uvc_camera_main, container, false);
        binding.setPresenter(new Presenter());
        return binding.getRoot();
    }

    @Override
    public void onBindService() {
    }

    @Override
    public void onUnbindService() {
    }

    public class Presenter {
        public void onClick() {

        }
    }
}
