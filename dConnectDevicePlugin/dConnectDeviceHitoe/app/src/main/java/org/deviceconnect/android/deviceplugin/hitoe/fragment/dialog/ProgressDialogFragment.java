/*
 ProgressDialogFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.fragment.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * This fragment displays a dialog of Progress.
 * @author NTT DOCOMO, INC.
 */
public class ProgressDialogFragment extends DialogFragment {
    /** Title's key. */
    private static final String PARAM_TITLE = "title";
    /** Message's key. */
    private static final String PARAM_MESSAGE = "message";
    /** Progress dialog. */
    private ProgressDialog mDialog;

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
        if (mDialog != null) {
            return mDialog;
        }

        String title = getArguments().getString(PARAM_TITLE);
        String message = getArguments().getString(PARAM_MESSAGE);

        mDialog = new ProgressDialog(getActivity());
        mDialog.setTitle(title);
        mDialog.setMessage(message);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        setCancelable(false);

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
}
