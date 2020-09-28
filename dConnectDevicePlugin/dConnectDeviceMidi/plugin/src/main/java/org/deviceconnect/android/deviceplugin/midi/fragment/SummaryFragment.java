/*
 SummaryFragment.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.deviceconnect.android.deviceplugin.midi.R;
import org.deviceconnect.android.deviceplugin.midi.ble.BleUtils;
import org.deviceconnect.android.deviceplugin.midi.fragment.dialog.ErrorDialogFragment;

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
        mErrorDialogFragment.setOnDismissListener((dialog) -> {
            mErrorDialogFragment = null;
            if (getActivity() != null) {
                getActivity().finish();
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
