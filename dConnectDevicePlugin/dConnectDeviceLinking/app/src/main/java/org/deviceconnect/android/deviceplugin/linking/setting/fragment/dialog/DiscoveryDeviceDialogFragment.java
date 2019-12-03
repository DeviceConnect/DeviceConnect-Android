/*
 DiscoveryDeviceDialogFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.setting.fragment.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class DiscoveryDeviceDialogFragment extends DialogFragment {

    private static final String EXTRA_MESSAGE = "message";

    public static DiscoveryDeviceDialogFragment newInstance(final String message) {
        DiscoveryDeviceDialogFragment instance = new DiscoveryDeviceDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_MESSAGE, message);
        instance.setArguments(arguments);
        return instance;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        String message = getArguments().getString(EXTRA_MESSAGE);
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        return progressDialog;
    }
}
