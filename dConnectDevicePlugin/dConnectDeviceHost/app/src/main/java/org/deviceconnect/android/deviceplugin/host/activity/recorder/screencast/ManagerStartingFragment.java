package org.deviceconnect.android.deviceplugin.host.activity.recorder.screencast;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.fragment.ManagerStartingConfirmationFragment;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

public class ManagerStartingFragment extends ManagerStartingConfirmationFragment {
    @Override
    public void onNextFragment() {
        findNavController(this).navigate(R.id.action_check_to_permission);
    }
}
