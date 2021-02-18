package org.deviceconnect.android.deviceplugin.uvc.fragment;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import org.deviceconnect.android.deviceplugin.uvc.R;
import org.deviceconnect.android.deviceplugin.uvc.databinding.FragmentUvcPermissionConfirmationBinding;
import org.deviceconnect.android.libmedia.streaming.util.PermissionUtil;

import java.util.List;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

public class PermissionConfirmationFragment extends UVCDevicePluginBindFragment {
    /**
     * パーミッションのリクエストコード.
     */
    private static final int PERMISSION_REQUEST_CODE = 21234;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentUvcPermissionConfirmationBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_uvc_permission_confirmation, container, false);
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

        List<String> denies = PermissionUtil.checkPermissions(context, getPermissions());
        if (denies.isEmpty()) {
            onNextFragment();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            List<String> denies = PermissionUtil.checkRequestPermissionsResult(permissions, grantResults);
            if (denies.isEmpty()) {
                onNextFragment();
            } else {
                onPermissionDeny();
            }
        }
    }

    /**
     * パーミッションを要求します.
     */
    public void onClickRequestPermissionsButton() {
        Context context = getContext();
        if (context == null) {
            return;
        }

        List<String> denies = PermissionUtil.checkPermissions(context, getPermissions());
        if (!denies.isEmpty()) {
            requestPermissions(denies.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * 許可を求めるパーミッションの配列を取得します.
     *
     * @return パーミッションの配列
     */
    public String[] getPermissions() {
        return new String[] {
                Manifest.permission.CAMERA
        };
    }

    /**
     * パーミッションの許可が降りている場合に次の画面に遷移します.
     */
    public void onNextFragment() {
        findNavController(this).navigate(R.id.action_permission_to_plugin);
    }

    /**
     * パーミッションの許可が降りなかった場合の処理を行います.
     */
    public void onPermissionDeny() {
        findNavController(this).navigate(R.id.action_permission_error_dialog);
    }
}