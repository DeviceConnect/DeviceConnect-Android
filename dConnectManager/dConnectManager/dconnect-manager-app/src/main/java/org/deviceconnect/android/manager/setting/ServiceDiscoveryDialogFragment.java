/*
 ServiceDiscoveryDialogFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.deviceconnect.android.manager.R;

/**
 * ServiceDiscovery実行中を表示するダイアログ.
 *
 * @author NTT DOCOMO, INC.
 */
public class ServiceDiscoveryDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        String title = getString(R.string.activity_service_list_search);
        String msg = getString(R.string.activity_service_list_service_discovery);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_progress, null);
        TextView titleView = v.findViewById(R.id.title);
        TextView messageView = v.findViewById(R.id.message);
        titleView.setText(title);
        messageView.setText(msg);
        builder.setView(v);

        return builder.create();
    }

    @Override
    public void onPause() {
        dismiss();
        super.onPause();
    }
}
