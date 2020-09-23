/*
 ErrorDialogFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import org.deviceconnect.android.deviceplugin.midi.R;

/**
 * This fragment displays a dialog of error.
 * @author NTT DOCOMO, INC.
 */
public class ErrorDialogFragment extends DialogFragment {
    private static final String PARAM_TITLE = "title";
    private static final String PARAM_MESSAGE = "message";
    private AlertDialog mDialog;
    private DialogInterface.OnDismissListener mListener;

    public static ErrorDialogFragment newInstance(final String title, final String message) {
        ErrorDialogFragment instance = new ErrorDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putString(PARAM_TITLE, title);
        arguments.putString(PARAM_MESSAGE, message);

        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        if (mDialog != null) {
            return mDialog;
        }

        String title = getArguments().getString(PARAM_TITLE);
        String message = getArguments().getString(PARAM_MESSAGE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok,
                (dialog, which) -> {
                    dismiss();
                });
        mDialog = builder.create();
        return mDialog;
    }

    @Override
    public Dialog getDialog() {
        return mDialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDialog = null;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mListener != null) {
            mListener.onDismiss(dialog);
        }
    }

    public void setOnDismissListener(final DialogInterface.OnDismissListener listener) {
        mListener = listener;
    }
}