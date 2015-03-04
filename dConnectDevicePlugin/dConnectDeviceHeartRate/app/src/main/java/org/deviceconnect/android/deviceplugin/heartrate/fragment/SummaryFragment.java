/*
 SummaryFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.fragment;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.deviceconnect.android.deviceplugin.heartrate.R;
import org.deviceconnect.android.deviceplugin.heartrate.ble.BleUtils;
import org.deviceconnect.android.deviceplugin.heartrate.fragment.dialog.ErrorDialogFragment;

/**
 * This fragment explain summary of this device plug-in.
 * @author NTT DOCOMO, INC.
 */
public class SummaryFragment extends Fragment {
    /**
     * Error Dialog.
     */
    private ErrorDialogFragment mErrorDialogFragment;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_summary, null);
        if (!BleUtils.isBLESupported(getActivity())) {
            showErrorDialog();
        }
        return rootView;
    }

    /**
     * Display the error dialog.
     */
    private void showErrorDialog() {
        dismissErrorDialog();

        Resources res = getActivity().getResources();
        String title = res.getString(R.string.summary_not_support_title);
        String message = res.getString(R.string.summary_not_support_message);
        mErrorDialogFragment = ErrorDialogFragment.newInstance(title, message);
        mErrorDialogFragment.show(getFragmentManager(), "error_dialog");
        mErrorDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mErrorDialogFragment = null;
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
    }

    /**
     * Dismiss the error dialog.
     */
    private void dismissErrorDialog() {
        if (mErrorDialogFragment != null) {
            mErrorDialogFragment.dismiss();
            mErrorDialogFragment = null;
        }
    }
}
