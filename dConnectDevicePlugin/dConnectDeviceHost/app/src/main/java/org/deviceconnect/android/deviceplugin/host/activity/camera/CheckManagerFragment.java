package org.deviceconnect.android.deviceplugin.host.activity.camera;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.libmedia.streaming.util.PermissionUtil;

import java.util.List;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

public class CheckManagerFragment extends CameraBaseFragment {

    /**
     * パーミッションのリクエストコード.
     */
    private static final int PERMISSION_REQUEST_CODE = 12345;

    /**
     * 使用するパーミッションのリスト.
     */
    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_host_camera_check_manager, container, false);
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
                findNavController(CheckManagerFragment.this).navigate(R.id.action_permission_error_dialog);
            } else {
                findNavController(CheckManagerFragment.this).navigate(R.id.action_check_to_main);
            }
        }
    }

    @Override
    public void onBindService() {
        List<String> denies =  PermissionUtil.checkPermissions(getContext(), PERMISSIONS);
        if (!denies.isEmpty()) {
            requestPermissions(denies.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            findNavController(CheckManagerFragment.this).navigate(R.id.action_check_to_main);
        }
    }

    @Override
    public void onUnbindService() {
    }

    private void startManager() {
        CameraActivity a = (CameraActivity) getActivity();
        if (a != null && !a.isManagerStarted()) {
            a.startManager();
        }
    }
}
