/*
 SettingTokenFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.slackmessagehook.setting.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

        // 次へボタン
        Button nextButton = (Button)root.findViewById(R.id.buttonNext);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText text = (EditText)root.findViewById(R.id.textToken);
                final String token = text.getText().toString();
                // プログレスダイアログを表示
                final ProgressDialog dialog = Utils.showProgressDialog(getActivity());

                // Token設定
                SlackManager.INSTANCE.setApiToken(token, true, new SlackManager.FinishCallback<Void>() {
                    @Override
                    public void onFinish(Void aVoid, Exception error) {
                        // プログレスダイアログを閉じる
                        dialog.dismiss();
                        if (error == null) {
                            // Tokenを保存
                            Utils.saveAccessToken(getActivity(), token);
                            // 画面遷移
                            Utils.transition(new SettingFragment(), getFragmentManager(), true);
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

        // トークン取得ボタン
        Button tokenButton = (Button)root.findViewById(R.id.buttonGetToken);
        tokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://slack.com/apps/A0F7YS25R-bots");
                Intent i = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(i);
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
