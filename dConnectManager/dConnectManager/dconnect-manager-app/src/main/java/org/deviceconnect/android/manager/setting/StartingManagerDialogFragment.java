/*
 StartingManagerDialogFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.deviceconnect.android.manager.R;

/**
 * Device Connect Manager 起動中を表示するダイアログ.
 *
 * @author NTT DOCOMO, INC.
 */
public class StartingManagerDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        String msg = getString(R.string.activity_service_list_launch_manager_message);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_manager_launch, null);
        TextView messageView = v.findViewById(R.id.message);
        messageView.setText(msg);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onPause() {
        dismiss();
        super.onPause();
    }
}
