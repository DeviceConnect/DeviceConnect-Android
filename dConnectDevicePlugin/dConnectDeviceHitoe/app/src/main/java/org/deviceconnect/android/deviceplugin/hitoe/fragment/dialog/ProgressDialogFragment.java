/*
 ProgressDialogFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.hitoe.R;

/**
 * This fragment displays a dialog of Progress.
 * @author NTT DOCOMO, INC.
 */
public class ProgressDialogFragment extends DialogFragment {
    /** Title's key. */
    private static final String PARAM_TITLE = "title";
    /** Message's key. */
    private static final String PARAM_MESSAGE = "message";
    /**
     * Initialize progress dialog.
     * @param title dialog's title
     * @param message dialog's message
     * @return progress dialog
     */
    public static ProgressDialogFragment newInstance(final String title, final String message) {
        ProgressDialogFragment instance = new ProgressDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putString(PARAM_TITLE, title);
        arguments.putString(PARAM_MESSAGE, message);

        instance.setArguments(arguments);

        return instance;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        String title = getArguments().getString(PARAM_TITLE);
        String message = getArguments().getString(PARAM_MESSAGE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_progress, null);
        TextView titleView = v.findViewById(R.id.title);
        TextView messageView = v.findViewById(R.id.message);
        titleView.setText(title);
        messageView.setText(message);
        builder.setView(v);
        setCancelable(false);
        return builder.create();
    }


}
