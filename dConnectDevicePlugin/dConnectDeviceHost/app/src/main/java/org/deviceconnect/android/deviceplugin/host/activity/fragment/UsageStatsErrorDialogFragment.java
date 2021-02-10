package org.deviceconnect.android.deviceplugin.host.activity.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import org.deviceconnect.android.deviceplugin.host.R;

public class UsageStatsErrorDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.host_error_usage_stats_title);
        builder.setMessage(R.string.host_error_usage_stats_message);
        builder.setPositiveButton(R.string.host_error_positive, (dialog, which) -> {
        });
        builder.setNegativeButton(R.string.host_error_negative, (dialog, which) -> {
            Activity a = getActivity();
            if (a != null) {
                a.finish();
            }
        });
        setCancelable(false);
        return builder.create();
    }
}