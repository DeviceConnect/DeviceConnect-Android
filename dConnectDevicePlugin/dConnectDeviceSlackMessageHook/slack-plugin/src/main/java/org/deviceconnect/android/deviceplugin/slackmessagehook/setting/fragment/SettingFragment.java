/*
 SettingFragment.java
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
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
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
    private View.OnClickListener onClickListener;
    /** 回転時の画面切り替え用にrootを保持しておく */
    private FrameLayout rootLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // メニュー対応
        setHasOptionsMenu(true);
        // Slackイベントを受け取る
        SlackManager.INSTANCE.addSlackEventListener(this);
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
    public void onDestroyView() {
        super.onDestroyView();
        // Slackイベントを受け取り解除
        SlackManager.INSTANCE.removeSlackEventListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStatus(getView());
        refreshDozePermission(getView());
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.setting_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        mainMenu = menu;
    }

    //endregion
    //---------------------------------------------------------------------------------------
    //region Private

    private void refreshDozePermission(final View view) {
        View dozeView = view.findViewById(R.id.doze_layout);
        if (dozeView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PowerManager mgr = getActivity().getSystemService(PowerManager.class);
                if (!mgr.isIgnoringBatteryOptimizations(getActivity().getPackageName())) {
                    Button dozeBtn = (Button) view.findViewById(R.id.doze_btn);
                    if (dozeBtn != null) {
                        dozeBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                    intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                                    getActivity().startActivity(intent);
                                }
                            }
                        });
                    }
                    dozeView.setVisibility(View.VISIBLE);
                } else {
                    dozeView.setVisibility(View.GONE);
                }
            } else {
                dozeView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * View作成
     * @return View
     */
    private View initView() {
        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.fragment_setting, null);
        final Context context = view.getContext();
        // Switchの設定
        final Switch sw = (Switch)view.findViewById(R.id.statusSwitch);
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // プログレスダイアログを表示
                final ProgressDialog dialog = Utils.showProgressDialog(getActivity());
                final SlackManager.FinishCallback<Boolean> finishCallback = new SlackManager.FinishCallback<Boolean>() {
                    @Override
                    public void onFinish(Boolean retry, Exception error) {
                        if (retry) {
                            // 再試行
                            onClickListener.onClick(v);
                        } else {
                            // 状態を更新
                            refreshStatus(view);
                        }
                    }
                };
                if (sw.isChecked()) {
                    // ネットワーク接続チェック
                    if (!Utils.onlineCheck(context)) {
                        // プログレスダイアログを閉じる
                        dialog.dismiss();
                        // エラー表示
                        Utils.showNetworkErrorDialog(context, finishCallback);
                        return;
                    }
                    // 接続
                    SlackManager.INSTANCE.connect(new SlackManager.FinishCallback<Void>() {
                        @Override
                        public void onFinish(Void aVoid, final Exception error) {
                            // プログレスダイアログを閉じる
                            dialog.dismiss();
                            // 状態を更新
                            refreshStatus(view);
                            if (error != null) {
                                if (error instanceof SlackManager.SlackAuthException) {
                                    // エラー表示
                                    Utils.showSlackAuthErrorDialog(context, getFragmentManager(), finishCallback);
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
                } else {
                    SlackManager.INSTANCE.disconnect(new SlackManager.FinishCallback<Void>() {
                        @Override
                        public void onFinish(Void aVoid, Exception error) {
                            // プログレスダイアログを閉じる
                            dialog.dismiss();
                            // 状態を更新
                            refreshStatus(view);
                        }
                    });
                }
            }
        };
        sw.setOnClickListener(onClickListener);
        refreshStatus(view);
        return view;
    }

    /**
     * 画面の状態を更新する
     */
    private void refreshStatus(View v) {
        // TeamNameの設定
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
            sw.setChecked(SlackManager.INSTANCE.isConnected());
        }
    }


    //endregion
    //---------------------------------------------------------------------------------------
    //region ShowMenuFragment

    /**
     * メニューを表示
     */
    public void showMenu() {
        mainMenu.performIdentifierAction(R.id.overflow_options, 0);
    }


    //endregion
    //---------------------------------------------------------------------------------------
    //region SlackEventListener

    @Override
    public void OnConnect() {
        refreshStatus(getView());
    }

    @Override
    public void OnConnectLost() {

    }

    @Override
    public void OnReceiveSlackMessage(SlackManager.HistoryInfo info) {

    }
}
