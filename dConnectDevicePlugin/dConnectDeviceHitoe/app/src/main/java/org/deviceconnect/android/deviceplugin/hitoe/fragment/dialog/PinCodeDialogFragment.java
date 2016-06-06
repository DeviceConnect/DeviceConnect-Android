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
import android.support.v4.app.DialogFragment;
import android.widget.EditText;

import org.deviceconnect.android.deviceplugin.hitoe.R;


/**
 * This fragment displays a dialog of Pincode input.
 * @author NTT DOCOMO, INC.
 */
public class PinCodeDialogFragment extends DialogFragment {
    private static final String PARAM_TITLE = "title";
    private static final String PARAM_MESSAGE = "message";
    private AlertDialog mDialog;
    private OnPinCodeListener mListener;

    public interface OnPinCodeListener {
        void onPinCode(final String pin);
    }


    public static PinCodeDialogFragment newInstance(final String title) {
        PinCodeDialogFragment instance = new PinCodeDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putString(PARAM_TITLE, title);

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
        final EditText pinEdit = new EditText(getActivity());
        builder.setView(pinEdit);
        builder.setPositiveButton(R.string.hitoe_setting_dialog_positive,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String pinString = pinEdit.getText().toString();
                        if (mListener != null) {
                            mListener.onPinCode(pinString);
                        }
                    }
                });
        builder.setNegativeButton("Cancel", null);
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
    }

    public void setOnPinCodeListener(final OnPinCodeListener listener) {
        mListener = listener;
    }
}