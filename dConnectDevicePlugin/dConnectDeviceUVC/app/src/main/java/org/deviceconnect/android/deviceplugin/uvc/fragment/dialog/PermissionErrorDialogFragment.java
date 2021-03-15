package org.deviceconnect.android.deviceplugin.uvc.fragment.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import org.deviceconnect.android.deviceplugin.uvc.R;

public class PermissionErrorDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.uvc_error_permission_title);
        builder.setMessage(R.string.uvc_error_permission_message);
        builder.setPositiveButton(R.string.uvc_error_positive, (dialog, which) -> {
            Activity a = getActivity();
            if (a != null) {
                a.finish();
            }
        });
        setCancelable(false);
        return builder.create();
    }
}
