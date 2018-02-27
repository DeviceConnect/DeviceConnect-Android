/*
 IRKitProgressDialogFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.settings.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.irkit.R;

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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_progress, null);
        TextView titleView = v.findViewById(R.id.title);
        TextView messageView = v.findViewById(R.id.message);
        titleView.setText(title);
        messageView.setText(message);
        builder.setView(v);
        setCancelable(false);
        return builder.create();
    }
}
