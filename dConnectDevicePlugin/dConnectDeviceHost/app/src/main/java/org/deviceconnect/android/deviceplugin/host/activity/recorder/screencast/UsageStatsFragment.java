package org.deviceconnect.android.deviceplugin.host.activity.recorder.screencast;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.fragment.UsageStatsConfirmationFragment;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

public class UsageStatsFragment extends UsageStatsConfirmationFragment {
    @Override
    public void onNextFragment() {
        findNavController(UsageStatsFragment.this).navigate(R.id.action_usages_stats_main);
    }

    @Override
    public void onDeny() {
        findNavController(UsageStatsFragment.this).navigate(R.id.action_usages_stats_error_dialog);
    }
}