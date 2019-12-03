/*
 ErrorDialogFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import org.deviceconnect.android.deviceplugin.hitoe.R;


/**
 * This fragment displays a dialog of Pincode input.
 * @author NTT DOCOMO, INC.
 */
public class PinCodeDialogFragment extends DialogFragment {
    /** Dialog. */
    private AlertDialog mDialog;
    /** Dialog's listener. */
    private OnPinCodeListener mListener;

    /**
     * Pin Code listener interface.
     */
    public interface OnPinCodeListener {
        /**
         * Notify pin code listener.
         * @param pin pin code
         */
        void onPinCode(final String pin);
    }

    /**
     * Initialize Pin code dialog.
     * @return pin code dialog
     */
    public static PinCodeDialogFragment newInstance() {
        return new PinCodeDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        if (mDialog != null) {
            return mDialog;
        }

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_hitoe_pin, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_title_pin_input));
        final EditText pinEdit = (EditText) layout.findViewById(R.id.input_pin);
        pinEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(layout);
        builder.setPositiveButton(R.string.hitoe_setting_dialog_positive,
                (dialogInterface, i) -> {
                    String pinString = pinEdit.getText().toString();
                    if (mListener != null) {
                        mListener.onPinCode(pinString);
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

    /**
     * Set Pin Code listener.
     * @param listener listener
     */
    public void setOnPinCodeListener(final OnPinCodeListener listener) {
        mListener = listener;
    }
}