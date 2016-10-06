/*
 SettingTokenFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.slackmessagehook.setting.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import org.deviceconnect.android.deviceplugin.slackmessagehook.R;
import org.deviceconnect.android.deviceplugin.slackmessagehook.setting.HelpActivity;
import org.deviceconnect.android.deviceplugin.slackmessagehook.slack.SlackManager;

/**
 * トークン設定画面用Fragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class SettingTokenFragment extends Fragment {

    /** 回転時の画面切り替え用にrootを保持しておく */
    private FrameLayout rootLayout;
    /** 次へボタンクリックリスナー */
    private View.OnClickListener onClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        // View作成
        View view = initView();
        rootLayout = new FrameLayout(view.getContext());
        rootLayout.addView(view);
        return rootLayout;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 画面回転時にレイアウトを作り直す
        rootLayout. removeAllViews();
        View view = initView();
        rootLayout.addView(view);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * View作成
     * @return View
     */
    private View initView() {
        // Root view.
        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.fragment_token, null);
        final Context context = view.getContext();

        // アクセストークン
        final EditText text = (EditText)view.findViewById(R.id.textToken);
        text.setText(Utils.getAccessToken(context));

        // 次へボタン
        Button nextButton = (Button)view.findViewById(R.id.buttonNext);
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final String token = text.getText().toString();
                // プログレスダイアログを表示
                final ProgressDialog dialog = Utils.showProgressDialog(context);
                final SlackManager.FinishCallback<Boolean> finishCallback = new SlackManager.FinishCallback<Boolean>() {
                    @Override
                    public void onFinish(Boolean retry, Exception error) {
                        if (retry) {
                            // 再試行
                            onClickListener.onClick(v);
                        }
                    }
                };

                // Token設定
                SlackManager.INSTANCE.setApiToken(token, true, new SlackManager.FinishCallback<Void>() {
                    @Override
                    public void onFinish(Void aVoid, Exception error) {
                        // プログレスダイアログを閉じる
                        dialog.dismiss();
                        if (error == null) {
                            // Tokenを保存
                            Utils.saveAccessToken(context, token);
                            // 画面遷移
                            Utils.transition(new SettingFragment(), getFragmentManager(), false);
                        } else {
                            // エラーダイアログ表示
                            if (error instanceof SlackManager.SlackAuthException ||
                                    error instanceof SlackManager.SlackAPITokenValueException) {
                                // エラー表示
                                Utils.showAlertDialog(context, context.getString(R.string.error_auth));
                            } else if (error instanceof SlackManager.SlackConnectionException) {
                                // エラー表示
                                Utils.showSlackErrorDialog(context, finishCallback);
                            } else {
                                // エラー表示
                                Utils.showErrorDialog(context, finishCallback);
                            }
                        }
                    }
                });
            }
        };
        nextButton.setOnClickListener(onClickListener);

        // トークン取得ボタン
        Button tokenButton = (Button)view.findViewById(R.id.buttonGetToken);
        tokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://slack.com/apps/A0F7YS25R-bots");
                Intent i = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(i);
            }
        });

        // ヘルプボタン
        Button helpButton = (Button)view.findViewById(R.id.buttonHelp);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HelpActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }
}
