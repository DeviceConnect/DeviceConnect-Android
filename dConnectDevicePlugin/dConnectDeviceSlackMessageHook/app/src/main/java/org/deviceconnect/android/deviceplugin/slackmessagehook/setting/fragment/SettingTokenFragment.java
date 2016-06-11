/*
 SettingTokenFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.slackmessagehook.setting.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.deviceconnect.android.deviceplugin.slackmessagehook.R;
import org.deviceconnect.android.deviceplugin.slackmessagehook.slack.SlackManager;

/**
 * トークン設定画面用Fragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class SettingTokenFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {

        // Root view.
        final View root = inflater.inflate(R.layout.token, container, false);

        Button button = (Button)root.findViewById(R.id.buttonNext);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText text = (EditText)root.findViewById(R.id.textToken);
                // プログレスダイアログを表示
                final ProgressDialog dialog = new ProgressDialog(root.getContext());
                dialog.setMessage("Please wait...");
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                // Token設定
                SlackManager.INSTANCE.setApiToken(text.getText().toString(), new SlackManager.FinishCallback<Void>() {
                    @Override
                    public void onFinish(Void aVoid, Exception error) {
                        // プログレスダイアログを閉じる
                        dialog.dismiss();
                        if (error == null) {
                            // TODO: Tokenを保存
                            // 画面遷移
                            FragmentManager manager = getFragmentManager();
                            FragmentTransaction transaction = manager.beginTransaction();
                            transaction.replace(R.id.container, new SettingFragment());
                            transaction.addToBackStack(null);
                            transaction.commit();
                        } else {
                            // エラーダイアログ表示
                            // TODO: 詳細なエラー表示
                            new AlertDialog.Builder(getActivity())
                                    .setTitle("エラー")
                                    .setMessage("エラーです")
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                });
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}
