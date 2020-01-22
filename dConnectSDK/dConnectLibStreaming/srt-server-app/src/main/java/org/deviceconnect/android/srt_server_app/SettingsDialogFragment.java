package org.deviceconnect.android.srt_server_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class SettingsDialogFragment extends DialogFragment {

    public interface SettingsDialogListener {
        void onSettingsDialogDismiss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("設定");
        dialog.setView(R.layout.activity_settings);
        dialog.setOnCancelListener((DialogInterface di) -> {
            Log.d("SRTServer", "OnCancelListener");
            Activity activity = getActivity();
            if (activity instanceof SettingsDialogListener) {
                ((SettingsDialogListener) activity).onSettingsDialogDismiss();
            }
        });
        dialog.setOnDismissListener((DialogInterface di) -> {
            Log.d("SRTServer", "OnDismissListener");
            Activity activity = getActivity();
            if (activity instanceof SettingsDialogListener) {
                ((SettingsDialogListener) activity).onSettingsDialogDismiss();
            }
        });
        dialog.setPositiveButton("OK", (DialogInterface dialogInterface, int i) -> {
            Log.d("SRTServer", "PositiveButton");
            Activity activity = getActivity();
            if (activity instanceof SettingsDialogListener) {
                ((SettingsDialogListener) activity).onSettingsDialogDismiss();
            }
        });
        dialog.setCancelable(false);
        return dialog.create();
    }
}
