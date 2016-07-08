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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
public class SettingFragment extends Fragment implements SlackManager.SlackEventListener, ShowMenuFragment {

    /** メニュー */
    private Menu mainMenu;
    /** Switchの設定変更イベントリスナー */
    private CompoundButton.OnCheckedChangeListener checkedChangeListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_setting, container, false);

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
                } else {
                    SlackManager.INSTANCE.disconnect(new SlackManager.FinishCallback<Void>() {
                        @Override
                        public void onFinish(Void aVoid, Exception error) {
                            // プログレスダイアログを閉じる
                            dialog.dismiss();
                            // 状態を更新
                            refreshStatus();
                        }
                    });
                }
            }
        };
        sw.setOnCheckedChangeListener(checkedChangeListener);

        // Slackイベントを受け取る
        SlackManager.INSTANCE.addSlackEventListener(this);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Slackイベントを受け取り解除
        SlackManager.INSTANCE.removeSlackEventListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStatus();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.setting_menu, menu);
        mainMenu = menu;
    }

    /**
     * メニューを表示
     */
    public void showMenu() {
        mainMenu.performIdentifierAction(R.id.overflow_options, 0);
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
                Utils.saveOnlineStatus(getActivity(), true);
            } else {
                teamText.setText("");
                Utils.saveOnlineStatus(getActivity(), false);
            }
            // Switchの設定
            Switch sw = (Switch) v.findViewById(R.id.statusSwitch);
            sw.setOnCheckedChangeListener(null);
            sw.setChecked(SlackManager.INSTANCE.isConnected());
            sw.setOnCheckedChangeListener(checkedChangeListener);
        }
    }

    @Override
    public void OnConnect() {
        refreshStatus();;
    }

    @Override
    public void OnReceiveSlackMessage(SlackManager.HistoryInfo info) {

    }
}
