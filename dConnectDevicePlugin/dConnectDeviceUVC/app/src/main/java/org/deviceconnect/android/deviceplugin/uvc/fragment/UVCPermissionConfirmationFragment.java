package org.deviceconnect.android.deviceplugin.uvc.fragment;

import org.deviceconnect.android.deviceplugin.uvc.R;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

public class UVCPermissionConfirmationFragment extends PermissionConfirmationFragment {
    @Override
    public void onNextFragment() {
        findNavController(this).navigate(R.id.action_permission_to_plugin);
    }

    @Override
    public void onPermissionDeny() {
        findNavController(this).navigate(R.id.action_permission_error_dialog);
    }
}
