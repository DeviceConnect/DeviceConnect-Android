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
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import org.deviceconnect.android.deviceplugin.hitoe.R;
import org.deviceconnect.android.deviceplugin.hitoe.util.UserSettings;

/**
 * Alert show fragment.
 * @author NTT DOCOMO, INC.
 */
public class DefaultDialogFragment extends DialogFragment {

    /**
     * Factory Method.
     * @param title dialog's title
     * @param message dialog's message
     * @return dialog fragment
     */
    public static DefaultDialogFragment newInstance(final String title,
                                                    final String message) {
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

    /**
     * Show Hitoe On state dialog.
     * @param activity activity
     */
    public static void showHitoeONStateDialog(final Activity activity) {
        if (activity == null) {
            return;
        }
        final UserSettings userSettings = new UserSettings(activity);
        if (userSettings.isNextState()) {
            return;
        }
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_hitoe_on, null);
        new AlertDialog.Builder(activity)
                .setView(layout)
                .setTitle(activity.getString(R.string.dialog_title_lunch_hitoe))
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, final int i) {
                        CheckBox nextState = (CheckBox) layout.findViewById(R.id.chceck_next);
                        userSettings.setNextState(nextState.isChecked());
                    }
                })
                .show();
    }

    /**
     * Show Hitoe set shirt dialog.
     * @param activity activity
     */
    public static void showHitoeSetShirtDialog(final Activity activity) {
        if (activity == null) {
            return;
        }
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_hitoe_set, null);
        new AlertDialog.Builder(activity)
                .setView(layout)
                .setTitle(activity.getString(R.string.dialog_title_equip_hitoe))
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface,final int i) {

                    }
                })
                .show();
    }


}
