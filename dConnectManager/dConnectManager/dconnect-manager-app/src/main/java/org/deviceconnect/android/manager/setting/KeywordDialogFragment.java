/*
 KeywordDialogFragment.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.fragment.app.DialogFragment;

import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.core.DConnectSettings;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

/**
 * キーワード表示用フラグメント.
 * 
 * @author NTT DOCOMO, INC.
 */
public class KeywordDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String keyword = sp.getString(getString(R.string.key_settings_dconn_keyword), DConnectSettings.DEFAULT_KEYWORD);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Dialog dialog = builder.setTitle(R.string.activity_keyword).setMessage(keyword)
                .setPositiveButton(R.string.activity_keyword_ok, (dialogs, which) -> {
                    dialogs.dismiss();
                }).create();
        return dialog;
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().finish();

        // dConnectManagerにダイアログを閉じたことを通知.
        Intent request = getActivity().getIntent();
        int requestCode = request.getIntExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, -1);
        if (requestCode == -1) {
            return;
        }
        Intent intent = new Intent();
        intent.setClass(getActivity(), DConnectService.class);
        intent.setAction(IntentDConnectMessage.ACTION_RESPONSE);
        intent.putExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, requestCode);
        try {
            getActivity().startService(intent);
        } catch (Exception e) {
            // ignore.
        }
    }
}
