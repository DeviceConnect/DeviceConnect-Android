/*
 SettingFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.slackmessagehook.setting.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.slackmessagehook.R;
import org.deviceconnect.android.deviceplugin.slackmessagehook.slack.SlackManager;

/**
 * 設定画面用Fragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class SettingFragment extends Fragment {

    /** Switchの設定変更イベントリスナー */
    private CompoundButton.OnCheckedChangeListener checkedChangeListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final Handler handler = new Handler();
        View root = inflater.inflate(R.layout.setting, container, false);

        // Switchの設定
        Switch sw = (Switch)root.findViewById(R.id.statusSwitch);
        checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // プログレスダイアログを表示
                final ProgressDialog dialog = Utils.showProgressDialog(getActivity());
                if (isChecked) {
                    // 接続
                    SlackManager.INSTANCE.connect(new SlackManager.FinishCallback<Void>() {
                        @Override
                        public void onFinish(Void aVoid, final Exception error) {
                            handler.post(new Runnable() {
                                public void run() {
                                    // プログレスダイアログを閉じる
                                    dialog.dismiss();
                                    // 状態を更新
                                    refreshStatus();
                                    if (error != null) {
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
                } else {
                    SlackManager.INSTANCE.disconnect(new SlackManager.FinishCallback<Void>() {
                        @Override
                        public void onFinish(Void aVoid, Exception error) {
                            handler.post(new Runnable() {
                                public void run() {
                                    // プログレスダイアログを閉じる
                                    dialog.dismiss();
                                    // 状態を更新
                                    refreshStatus();
                                }
                            });
                        }
                    });
                }
            }
        };
        sw.setOnCheckedChangeListener(checkedChangeListener);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStatus();;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * 画面の状態を更新する
     */
    private void refreshStatus() {
        // TeamNameの設定
        View v = getView();
        if (v != null) {
            TextView teamText = (TextView) v.findViewById(R.id.teamText);
            if (SlackManager.INSTANCE.isConnected()) {
                String name = SlackManager.INSTANCE.getBotInfo().teamDomain;
                teamText.setText(name + ".slack.com");
            } else {
                teamText.setText("");
            }
            // Switchの設定
            Switch sw = (Switch) v.findViewById(R.id.statusSwitch);
            sw.setOnCheckedChangeListener(null);
            sw.setChecked(SlackManager.INSTANCE.isConnected());
            sw.setOnCheckedChangeListener(checkedChangeListener);
        }
    }
}
