package org.deviceconnect.android.deviceplugin.host.activity.camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class PermissionErrorDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("パーミッションエラー");
        builder.setMessage("カメラ映像を配信するためのパーミッションが許可されなかったので、アプリケーションを終了します。");
        builder.setPositiveButton("はい", (dialog, which) -> {
            Activity a = getActivity();
            if (a != null) {
                a.finish();
            }
        });
        setCancelable(false);
        return builder.create();
    }
}
