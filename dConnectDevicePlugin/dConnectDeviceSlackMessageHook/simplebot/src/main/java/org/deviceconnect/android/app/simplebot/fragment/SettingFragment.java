/*
 SettingFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.app.simplebot.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;

import org.deviceconnect.android.app.simplebot.R;
import org.deviceconnect.android.app.simplebot.SimpleBotService;
import org.deviceconnect.android.app.simplebot.data.SettingData;
import org.deviceconnect.android.app.simplebot.utils.DConnectHelper;
import org.deviceconnect.android.app.simplebot.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 設定画面
 */
public class SettingFragment extends Fragment implements ShowMenuFragment {

    /** 選択サービス */
    private DConnectHelper.ServiceInfo selectedInfo = null;
    /** サービス停止のレシーバー */
    private BroadcastReceiver receiver;
    /** メニュー */
    private Menu mainMenu;

    private Switch switchStatus;
    private EditText editTextHost;
    private EditText editTextPort;
    private CheckBox checkBoxSSL;
    private Button buttonService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        final View view = inflater.inflate(R.layout.fragment_setting, container, false);
        final Context context = view.getContext();

        switchStatus = (Switch)view.findViewById(R.id.switchStatus);
        editTextHost = (EditText)view.findViewById(R.id.editTextHost);
        editTextPort = (EditText)view.findViewById(R.id.editTextPort);
        checkBoxSSL = (CheckBox)view.findViewById(R.id.checkBoxSSL);
        buttonService = (Button)view.findViewById(R.id.buttonService);

        // サービス停止のレシーバー登録
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // チェックを入れていたのにサービスが止まった時はエラーメッセージ表示
                if (switchStatus.isChecked()) {
                    // エラーメッセージ
                    Utils.showAlertDialog(context, getString(R.string.err_occurred));
                    // チェックを外す
                    switchStatus.setChecked(false);
                    // UI更新
                    SettingData setting = SettingData.getInstance(getActivity());
                    changeEnabled(setting);
                }
            }
        };
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SimpleBotService.SERVICE_STOP_ACTION);
        getActivity().registerReceiver(receiver, intentFilter);

        // サービス選択ボタンイベント
        buttonService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 設定保存
                saveSettings();
                // サービス取得
                fetchServices(context);
            }
        });

        // ステータススイッチイベント
        switchStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedInfo != null) {
                    // 保存
                    saveSettings();
                    Intent serviceIntent = new Intent(context, SimpleBotService.class);
                    if (switchStatus.isChecked()) {
                        // サービス開始
                        context.startService(serviceIntent);
                    } else {
                        // サービス停止
                        context.stopService(serviceIntent);
                    }
                } else {
                    if (switchStatus.isChecked()) {
                        // エラーメッセージ
                        Utils.showAlertDialog(context, getString(R.string.service_not_selected));
                        // チェックを外す
                        switchStatus.setChecked(false);
                    }
                }
            }
        });

        // 設定読み込み
        loadSettings();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // レシーバー解除
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getString(R.string.app_name) + " [設定]");
        refreshDozePermission(getView());
    }

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
     * 設定読み込み
     */
    private void loadSettings() {
        SettingData setting = SettingData.getInstance(getActivity());
        editTextHost.setText(setting.host);
        editTextPort.setText(String.valueOf(setting.port));
        checkBoxSSL.setChecked(setting.ssl);
        switchStatus.setChecked(setting.active);
        if (setting.serviceId != null) {
            selectedInfo = new DConnectHelper.ServiceInfo(setting.serviceId, setting.serviceName, null);
            buttonService.setText(selectedInfo.name);
        }
        // UI更新
        changeEnabled(setting);
    }

    /**
     * 設定保存
     */
    private void saveSettings() {
        SettingData setting = SettingData.getInstance(getActivity());
        setting.host = editTextHost.getText().toString();
        String port = editTextPort.getText().toString();
        if (port.length() > 0) {
            setting.port = Integer.parseInt(port);
        }
        setting.ssl = checkBoxSSL.isChecked();
        setting.active = switchStatus.isChecked();
        if (selectedInfo != null) {
            setting.serviceId = selectedInfo.id;
            setting.serviceName = selectedInfo.name;
        }
        setting.save();
        // UI更新
        changeEnabled(setting);
    }

    /**
     * UI使用可能を切り替える
     */
    private void changeEnabled(SettingData setting) {
        editTextHost.setEnabled(!setting.active);
        editTextPort.setEnabled(!setting.active);
        checkBoxSSL.setEnabled(!setting.active);
        buttonService.setEnabled(!setting.active);
    }

    /**
     * サービス一覧を取得
     * @param context context
     */
    private void fetchServices(final Context context) {
        // プログレスダイアログを表示
        final ProgressDialog dialog = Utils.showProgressDialog(context);
        // サービス取得
        Utils.fetchServices(context, new DConnectHelper.FinishCallback<List<DConnectHelper.ServiceInfo>>() {
            @Override
            public void onFinish(final List<DConnectHelper.ServiceInfo> serviceInfos, final Exception error) {
                // プログレスダイアログを閉じる
                dialog.dismiss();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkFetchServices(serviceInfos, error, context);
                    }
                });
            }
        });
    }

    private void checkFetchServices(List<DConnectHelper.ServiceInfo> serviceInfos, Exception error, Context context) {
        if (error == null) {
            // messageHookに対応しているサービスを選別
            final List<DConnectHelper.ServiceInfo> services = new ArrayList<>();
            for (DConnectHelper.ServiceInfo service : serviceInfos) {
                if (service.scopes != null) {
                    for (String scope : service.scopes) {
                        if ("messageHook".equalsIgnoreCase(scope)) {
                            services.add(service);
                            break;
                        }
                    }
                }
            }
            if (services.size() > 0) {
                // 選択ダイアログ表示
                String items[] = new String[services.size()];
                for (int i = 0; i < services.size(); i++) {
                    items[i] = services.get(i).name;
                }
                new AlertDialog.Builder(context)
                        .setTitle(getString(R.string.select_service))
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 情報取得
                                selectedInfo = services.get(which);
                                buttonService.setText(selectedInfo.name);
                            }
                        }).show();
            } else {
                selectedInfo = null;
                buttonService.setText(getString(R.string.unset));
                Utils.showAlertDialog(context, getString(R.string.service_not_found_and_install));
            }
        } else {
            // エラー処理
            Utils.showErrorDialog(context, error);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_list_command:
                Fragment fragment = new CommandListFragment();
                Utils.transition(fragment, getFragmentManager(), true);
                break;
            case R.id.menu_list_result:
                fragment = new ResultListFragment();
                Utils.transition(fragment, getFragmentManager(), true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
        mainMenu = menu;
    }

    /**
     * メニューを表示
     */
    public void showMenu() {
        mainMenu.performIdentifierAction(R.id.overflow_options, 0);
    }
}
