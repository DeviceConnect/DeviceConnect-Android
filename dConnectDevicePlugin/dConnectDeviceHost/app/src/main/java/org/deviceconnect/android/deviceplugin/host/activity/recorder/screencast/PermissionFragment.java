package org.deviceconnect.android.deviceplugin.host.activity.recorder.screencast;

import android.Manifest;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.fragment.PermissionConfirmationFragment;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

public class PermissionFragment extends PermissionConfirmationFragment {
    @Override
    public String[] getPermissions() {
        return new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
        };
    }

    @Override
    public void onNextFragment() {
        findNavController(PermissionFragment.this).navigate(R.id.action_permission_to_usages_stats);
    }

    @Override
    public void onPermissionDeny() {
        findNavController(PermissionFragment.this).navigate(R.id.action_permission_error_dialog);
    }
}