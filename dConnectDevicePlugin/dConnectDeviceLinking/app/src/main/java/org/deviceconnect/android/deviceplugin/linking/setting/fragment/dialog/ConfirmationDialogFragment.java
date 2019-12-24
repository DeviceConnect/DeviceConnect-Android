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
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

public class ConfirmationDialogFragment extends DialogFragment {

    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_MESSAGE = "message";
    private static final String EXTRA_POSITIVE = "positive";
    private static final String EXTRA_NEGATIVE = "negative";

    public static ConfirmationDialogFragment newInstance(
            final String title, final String message, final String positive,
            final String negative, final Fragment fragment) {
        Bundle args = new Bundle();
        args.putString(EXTRA_TITLE, title);
        args.putString(EXTRA_MESSAGE, message);
        if (positive != null) {
            args.putString(EXTRA_POSITIVE, positive);
        }
        if (negative != null) {
            args.putString(EXTRA_NEGATIVE, negative);
        }

        ConfirmationDialogFragment f = new ConfirmationDialogFragment();
        f.setArguments(args);
        if (fragment != null) {
            f.setTargetFragment(fragment, 0);
        }
        return f;
    }

    public static ConfirmationDialogFragment newInstance(
            final String title, final String message, final String positive, final String negative) {
        return newInstance(title, message, positive, negative, null);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        String title = getArguments().getString(EXTRA_TITLE);
        String message = getArguments().getString(EXTRA_MESSAGE);
        String positive = getArguments().getString(EXTRA_POSITIVE);
        String negative = getArguments().getString(EXTRA_NEGATIVE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        if (positive != null) {
            builder.setPositiveButton(positive, (dialog, id) -> {
                OnDialogEventListener l = getOnDialogEventListener();
                if (l != null) {
                    l.onPositiveClick(ConfirmationDialogFragment.this);
                }
            });
        }
        if (negative != null) {
            builder.setNegativeButton(negative, (dialog, id) -> {
                OnDialogEventListener l = getOnDialogEventListener();
                if (l != null) {
                    l.onNegativeClick(ConfirmationDialogFragment.this);
                }
            });
        }
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
        void onPositiveClick(DialogFragment fragment);
        void onNegativeClick(DialogFragment fragment);
    }
}
