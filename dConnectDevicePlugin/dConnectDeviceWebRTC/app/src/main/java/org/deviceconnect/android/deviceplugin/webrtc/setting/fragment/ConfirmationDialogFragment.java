package org.deviceconnect.android.deviceplugin.webrtc.setting.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import org.deviceconnect.android.deviceplugin.webrtc.R;

public class ConfirmationDialogFragment extends DialogFragment {

    private String mTitle;
    private String mMessage;

    private OnConfirmationListener mOnConfirmationListener;

    public static ConfirmationDialogFragment create(final String title, final String message) {
        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment();
        dialog.setTitle(title);
        dialog.setMessage(message);
        return dialog;
    }

    private void setTitle(final String title) {
        mTitle = title;
    }

    private void setMessage(final String message) {
        mMessage = message;
    }

    public void setOnConfirmationListener(final OnConfirmationListener listener) {
        mOnConfirmationListener = listener;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle);
        builder.setMessage(mMessage);
        builder.setPositiveButton(R.string.settings_dialog_btn_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                if (mOnConfirmationListener != null) {
                    mOnConfirmationListener.onPositive();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.settings_dialog_btn_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                if (mOnConfirmationListener != null) {
                    mOnConfirmationListener.onNegative();
                }
                dialog.dismiss();
            }
        });
        return builder.create();
    }


    public interface OnConfirmationListener {
        void onPositive();
        void onNegative();
    }
}
