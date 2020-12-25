package org.deviceconnect.android.deviceplugin.host.activity.camera;

import android.Manifest;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.fragment.ManagerStartingConfirmationFragment;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

public class ManagerStartingFragment extends ManagerStartingConfirmationFragment {
    @Override
    public String[] getPermissions() {
        return new String[] {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }

    @Override
    public void onNextFragment() {
        findNavController(ManagerStartingFragment.this).navigate(R.id.action_check_to_main);
    }

    @Override
    public void onPermissionDeny() {
        findNavController(ManagerStartingFragment.this).navigate(R.id.action_permission_error_dialog);
    }
}
