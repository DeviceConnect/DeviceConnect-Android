/*
 ConfirmationDialogFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.setting.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

public class ConfirmationDialogFragment extends DialogFragment {

    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_MESSAGE = "message";
    private static final String EXTRA_POSITIVE = "positive";
    private static final String EXTRA_NEGATIVE = "negative";

    public static ConfirmationDialogFragment newInstance(String title, String message, String positive, String negative, Fragment fragment) {
        Bundle args = new Bundle();
        args.putString(EXTRA_TITLE, title);
        args.putString(EXTRA_MESSAGE, message);
        args.putString(EXTRA_POSITIVE, positive);
        args.putString(EXTRA_NEGATIVE, negative);

        ConfirmationDialogFragment f = new ConfirmationDialogFragment();
        f.setArguments(args);
        if (fragment != null) {
            f.setTargetFragment(fragment, 0);
        }
        return f;
    }

    public static ConfirmationDialogFragment newInstance(String title, String message, String positive, String negative) {
        return newInstance(title, message, positive, negative, null);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(EXTRA_TITLE);
        String message = getArguments().getString(EXTRA_MESSAGE);
        String positive = getArguments().getString(EXTRA_POSITIVE);
        String negative = getArguments().getString(EXTRA_NEGATIVE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message)
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        OnDialogEventListener l = getOnDialogEventListener();
                        if (l != null) {
                            l.onPositiveClick();
                        }
                    }
                })
                .setNegativeButton(negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        OnDialogEventListener l = getOnDialogEventListener();
                        if (l != null) {
                            l.onNegativeClick();
                        }
                    }
                });
        return builder.create();
    }

    private OnDialogEventListener getOnDialogEventListener() {
        try {
            if (getTargetFragment() != null) {
                return (OnDialogEventListener) getTargetFragment();
            }
        } catch (ClassCastException e) {
            // do nothing.
        }
        try {
            return (OnDialogEventListener) getActivity();
        } catch (ClassCastException e) {
            return null;
        }
    }

    public interface OnDialogEventListener {
        void onPositiveClick();
        void onNegativeClick();
    }
}
