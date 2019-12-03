/*
 ErrorDialogFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import org.deviceconnect.android.deviceplugin.hitoe.R;


/**
 * This fragment displays a dialog of error.
 * @author NTT DOCOMO, INC.
 */
public class ErrorDialogFragment extends DialogFragment {
    /**
     * Title's key.
     */
    private static final String PARAM_TITLE = "title";
    /**
     * Message's key.
     */
    private static final String PARAM_MESSAGE = "message";
    /** dialog. */
    private AlertDialog mDialog;
    /** dialog's listener. */
    private DialogInterface.OnDismissListener mListener;

    /**
     * Initialize error dialog.
     * @param title dialog's title
     * @param message dialog's message
     * @return error dialog
     */
    public static ErrorDialogFragment newInstance(final String title, final String message) {
        ErrorDialogFragment instance = new ErrorDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putString(PARAM_TITLE, title);
        arguments.putString(PARAM_MESSAGE, message);

        instance.setArguments(arguments);

        return instance;
    }
    @NonNull
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
        builder.setPositiveButton(R.string.hitoe_setting_dialog_positive,
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

    /**
     * Set listener.
     * @param listener listener
     */
    public void setOnDismissListener(final DialogInterface.OnDismissListener listener) {
        mListener = listener;
    }
}
