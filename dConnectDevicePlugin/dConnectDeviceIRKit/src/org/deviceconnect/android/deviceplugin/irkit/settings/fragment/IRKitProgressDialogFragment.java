/*
 IRKitProgressDialogFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.settings.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

/**
 * プログレスバーを表示するダイアログ.
 * @author NTT DOCOMO, INC.
 */
public class IRKitProgressDialogFragment extends DialogFragment {

    /**
     * ファクトリーメソッド.
     */
    public static IRKitProgressDialogFragment newInstance(String title, String message){
        IRKitProgressDialogFragment instance = new IRKitProgressDialogFragment();

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
}
