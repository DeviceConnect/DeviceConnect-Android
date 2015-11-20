/*
 ThetaProgressDialogFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.theta.R;

/**
 * Alert show fragment.
 * @author NTT DOCOMO, INC.
 */
public class ThetaDialogFragment extends DialogFragment {

    /**
     * Factory Method.
     */
    public static ThetaDialogFragment newInstance(String title, String message){
        ThetaDialogFragment instance = new ThetaDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putString("title", title);
        arguments.putString("message", message);

        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        String message = getArguments().getString("message");

        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        setCancelable(false);

        return progressDialog;
    }

    /**
     * Show Alert.
     * @param activity Activity
     * @param title title
     * @param message message
     */
    public static void showAlert(final Activity activity, final String title, final String message,
                                 final DialogInterface.OnClickListener listener) {
        if (activity == null) {
            return;
        }

        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    /**
     * Show Confirm Alert.
     * @param activity Activity
     * @param title title
     * @param message message
     * @param positiveBtnMsg Positive Button Message
     * @param listener listener
     */
    public static void showConfirmAlert(final Activity activity, final String title, final String message,
                                        final String positiveBtnMsg,
                                        final DialogInterface.OnClickListener listener) {
        if (activity == null) {
            return;
        }

        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveBtnMsg, listener)
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    /**
     * Show Reconnection Prompt Dialog.
     * @param activity Activity
     * @param positive positive button listener
     * @param negative negative button listener
     */
    public static void showReconnectionDialog(final Activity activity,
                                              final DialogInterface.OnClickListener positive,
                                              final DialogInterface.OnClickListener negative) {
        if (activity == null) {
            return;
        }

        new AlertDialog.Builder(activity)
            .setTitle(R.string.communication_error)
            .setMessage(R.string.theta_error_reconnection_dialog_message)
            .setPositiveButton(R.string.open_settings, positive)
            .setNegativeButton(R.string.button_cancel, negative)
            .show();
    }

    /**
     * Show Disconnection Dialog.
     * @param activity Activity
     * @param positive positive button listener
     * @param negative negative button listener
     */
    public static void showDisconnectionDialog(final Activity activity,
                                               final DialogInterface.OnClickListener positive,
                                               final DialogInterface.OnClickListener negative) {
        if (activity == null) {
            return;
        }

        new AlertDialog.Builder(activity)
            .setTitle(R.string.communication_error)
            .setMessage(R.string.theta_error_disconnect_dialog_message)
            .setPositiveButton(R.string.open_settings, positive)
            .setNegativeButton(R.string.button_cancel, negative)
            .show();
    }
}
