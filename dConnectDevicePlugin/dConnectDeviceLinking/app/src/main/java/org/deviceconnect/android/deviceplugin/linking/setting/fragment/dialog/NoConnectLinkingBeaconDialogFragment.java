/*
 org.deviceconnect.android.deviceplugin.linking
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.setting.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import org.deviceconnect.android.deviceplugin.linking.R;

public class NoConnectLinkingBeaconDialogFragment extends DialogFragment {

    public static NoConnectLinkingBeaconDialogFragment newInstance(Fragment fragment) {
        NoConnectLinkingBeaconDialogFragment f = new NoConnectLinkingBeaconDialogFragment();
        f.setTargetFragment(fragment, 0);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getString(R.string.fragment_beacon_error_title);
        String message = getString(R.string.fragment_beacon_error_message);
        String positive = getString(R.string.fragment_beacon_error_positive);
        String negative = getString(R.string.fragment_beacon_error_negative);

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
            return (OnDialogEventListener) getTargetFragment();
        } catch (ClassCastException e) {
            return null;
        }
    }

    public interface OnDialogEventListener {
        void onPositiveClick();

        void onNegativeClick();
    }
}
