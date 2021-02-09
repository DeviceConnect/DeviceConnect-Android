package org.deviceconnect.android.deviceplugin.host.activity.recorder.camera;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.fragment.ManagerStartingConfirmationFragment;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

public class ManagerStartingFragment extends ManagerStartingConfirmationFragment {
    @Override
    public void onNextFragment() {
        findNavController(ManagerStartingFragment.this).navigate(R.id.action_check_to_permission);
    }
}
