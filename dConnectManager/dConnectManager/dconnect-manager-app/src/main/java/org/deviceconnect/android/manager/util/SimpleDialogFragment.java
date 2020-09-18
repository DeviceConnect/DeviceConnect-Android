package org.deviceconnect.android.manager.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;

public class SimpleDialogFragment extends DialogFragment {

    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_MESSAGE = "message";
    private static final String EXTRA_POSITIVE = "positive";
    private static final String EXTRA_NEGATIVE = "negative";
    private static final String EXTRA_CANCELABLE = "cancelable";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Bundle args = getArguments();
        if (args != null) {
            String title = args.getString(EXTRA_TITLE);
            if (title != null) {
                builder.setTitle(title);
            }

            String message = args.getString(EXTRA_MESSAGE);
            if (message != null) {
                builder.setMessage(message);
            }

            String positive = args.getString(EXTRA_POSITIVE);
            if (positive != null) {
                builder.setPositiveButton(positive, (dialogInterface, i)
                        -> postOnPositiveButtonClicked(SimpleDialogFragment.this));
            }

            String negative = args.getString(EXTRA_NEGATIVE);
            if (negative != null) {
                builder.setNegativeButton(negative, (dialogInterface, i)
                        -> postOnNegativeButtonClicked(SimpleDialogFragment.this));
            }

            setCancelable(args.getBoolean(EXTRA_CANCELABLE));
        }

        return builder.create();
    }

    @Override
    public void onDestroyView() {
        dismiss();
        super.onDestroyView();
    }

    private void postOnPositiveButtonClicked(SimpleDialogFragment dialog) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.onDialogPositiveButtonClicked(dialog);
        }
    }

    private void postOnNegativeButtonClicked(SimpleDialogFragment dialog) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.onDialogNegativeButtonClicked(dialog);
        }
    }

    private Callback getCallback() {
        Fragment f = getTargetFragment();
        if (f instanceof Callback) {
            return (Callback) f;
        }

        Activity a = getActivity();
        if (a instanceof Callback) {
           return (Callback) a;
        }

        return null;
    }

    public static class Builder {
        private String mTitle;
        private String mMessage;
        private String mPositive;
        private String mNegative;
        private Fragment mTargetFragment;
        private int mRequestCode;
        private boolean mCancelable = false;

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setMessage(String message) {
            mMessage = message;
            return this;
        }

        public Builder setPositive(String positive) {
            mPositive = positive;
            return this;
        }

        public Builder setNegative(String negative) {
            mNegative = negative;
            return this;
        }

        public Builder setTargetFragment(Fragment targetFragment) {
            mTargetFragment = targetFragment;
            return this;
        }

        public Builder setRequestCode(int requestCode) {
            mRequestCode = requestCode;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            mCancelable = cancelable;
            return this;
        }

        public SimpleDialogFragment create() {
            Bundle args = new Bundle();
            if (mTitle != null) {
                args.putString(EXTRA_TITLE, mTitle);
            }

            if (mMessage != null) {
                args.putString(EXTRA_MESSAGE, mMessage);
            }

            if (mPositive != null) {
                args.putString(EXTRA_POSITIVE, mPositive);
            }

            if (mNegative != null) {
                args.putString(EXTRA_NEGATIVE, mNegative);
            }

            args.putBoolean(EXTRA_CANCELABLE, mCancelable);

            SimpleDialogFragment fragment = new SimpleDialogFragment();
            fragment.setArguments(args);

            if (mTargetFragment != null) {
                fragment.setTargetFragment(mTargetFragment, mRequestCode);
            }

            return fragment;
        }
    }


    public interface Callback {
        void onDialogPositiveButtonClicked(SimpleDialogFragment dialog);
        void onDialogNegativeButtonClicked(SimpleDialogFragment dialog);
    }
}
