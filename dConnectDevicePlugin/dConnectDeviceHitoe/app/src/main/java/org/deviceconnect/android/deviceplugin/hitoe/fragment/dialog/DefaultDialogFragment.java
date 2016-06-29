/*
 DefaultDialogFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.fragment.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.hitoe.R;

/**
 * Alert show fragment.
 * @author NTT DOCOMO, INC.
 */
public class DefaultDialogFragment extends DialogFragment {

    /**
     * Factory Method.
     */
    public static DefaultDialogFragment newInstance(String title, String message){
        DefaultDialogFragment instance = new DefaultDialogFragment();

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
                .setNegativeButton(R.string.cancel, null)
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
        builder.setNegativeButton(R.string.cancel, negativeListener);
        builder.setCancelable(true);
        builder.show();
    }

}
