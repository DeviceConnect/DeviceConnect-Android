/*
 HVCC2WDialogFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcc2w.setting.fragment;

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

import org.deviceconnect.android.deviceplugin.hvcc2w.R;


/**
 * Alert show fragment.
 * @author NTT DOCOMO, INC.
 */
public class HVCC2WDialogFragment extends DialogFragment {

    /**
     * Factory Method.
     */
    public static HVCC2WDialogFragment newInstance(String title, String message){
        HVCC2WDialogFragment instance = new HVCC2WDialogFragment();

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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_progress, null);
        TextView titleView = (TextView) v.findViewById(R.id.title);
        TextView messageView = (TextView) v.findViewById(R.id.message);
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
                .setPositiveButton(R.string.button_ok, listener)
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


    /**
     * Show Select Dialog.
     * @param activity Activity
     * @param title Dialog's title
     * @param list Dialog's select
     * @param singleChoiceListner choice listener
     * @param positiveListener click listener
     * @param negativeListener click listener
     */
    public static void showSelectDialog(final Activity activity,
                                        final String title,
                                        final String[] list,
                                        final DialogInterface.OnClickListener singleChoiceListner,
                                        final DialogInterface.OnClickListener positiveListener,
                                        final DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(title);
        builder.setSingleChoiceItems(list, 0, singleChoiceListner);
        builder.setPositiveButton(R.string.button_ok, positiveListener);
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
        builder.setTitle(R.string.c2w_password);
        builder.setView(editView).setPositiveButton(R.string.button_ok, okListener);
        builder.setNegativeButton(R.string.button_cancel, cancelListener);
        builder.show();
    }
}
