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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.theta.R;

/**
 * Alert show fragment.
 * @author NTT DOCOMO, INC.
 */
public class ThetaDialogFragment extends DialogFragment {
    private static final String PARAM_TITLE = "title";
    private static final String PARAM_MESSAGE = "message";
    /**
     * Factory Method.
     */
    public static ThetaDialogFragment newInstance(String title, String message){
        ThetaDialogFragment instance = new ThetaDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putString(PARAM_TITLE, title);
        arguments.putString(PARAM_MESSAGE, message);

        instance.setArguments(arguments);

        return instance;
    }

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

        return builder.create();
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
                .setCancelable(false)
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

    public static void showSelectCommandDialog(final Activity activity,
                                               final String[] commands,
                                               final DialogInterface.OnClickListener selected) {
        if (activity == null) {
            return;
        }
        new AlertDialog.Builder(activity)
                .setItems(commands, selected)
                .show();
    }


    public static void showSelectWifiDialog(final Activity activity,
                                            final String title,
                                            final String[] list,
                                            final DialogInterface.OnClickListener singleChoiceListner,
                                            final DialogInterface.OnClickListener positiveListener,
                                            final DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(title);
        builder.setSingleChoiceItems(list, 0, singleChoiceListner);
        builder.setPositiveButton(R.string.ok, positiveListener);
        builder.setNegativeButton(R.string.button_cancel, negativeListener);
        builder.setCancelable(true);
        builder.show();
    }

    public static void showPasswordDialog(final Activity activity,
                                          final EditText editView,
                                    final DialogInterface.OnClickListener okListener,
                                    final DialogInterface.OnClickListener cancelListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(R.string.theta_password_dialog);
        builder.setView(editView).setPositiveButton(R.string.ok, okListener);
        builder.setNegativeButton(R.string.button_cancel, cancelListener);
        builder.show();
    }
}
