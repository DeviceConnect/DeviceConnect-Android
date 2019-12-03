/*
 HOGPSettingActivity.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.hogp.HOGPMessageService;
import org.deviceconnect.android.deviceplugin.hogp.HOGPService;
import org.deviceconnect.android.deviceplugin.hogp.HOGPSetting;
import org.deviceconnect.android.deviceplugin.hogp.R;
import org.deviceconnect.android.deviceplugin.hogp.server.AbstractHOGPServer;
import org.deviceconnect.android.deviceplugin.hogp.server.HOGPServer;
import org.deviceconnect.android.deviceplugin.hogp.util.BleUtils;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 設定画面用Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class HOGPSettingActivity extends HOGPBaseActivity implements DConnectServiceListener {

    /**
     * デバイスの一覧を格納するアダプタ.
     */
    private DeviceAdapter mDeviceAdapter;

    /**
     * ハンドラー.
     */
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = new Intent();
        intent.setClass(this, HOGPMessageService.class);
        startService(intent);

        findViewById(R.id.activity_setting_device_switch).setEnabled(false);
        findViewById(R.id.activity_setting_btn).setEnabled(false);
        findViewById(R.id.activity_setting_btn).setOnClickListener((v) -> {
            if (isStartHOGPServer()) {
                openControlActivity();
            } else {
                showNotStartServer();
            }
        });

        mDeviceAdapter = new DeviceAdapter();

        ListView listView = findViewById(R.id.activity_setting_list_view);
        listView.setAdapter(mDeviceAdapter);

        setDeviceName();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setDeviceName();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_setting_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.activity_setting_menu_help) {
            openHelpActivity();
            return false;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BleUtils.REQUEST_CODE_BLUETOOTH_ENABLE) {
            if (resultCode == RESULT_OK) {
                setDeviceName();

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startHOGPServer();
                    }
                }, 300);
            }
        }
    }

    @Override
    void onServiceConnected() {
        setHOGPServerUI();
        setLocalOAuthUI();
        findViewById(R.id.activity_setting_btn).setEnabled(true);

        HOGPMessageService service = getHOGPMessageService();
        if (service != null) {
            service.getServiceProvider().addServiceListener(this);
        }
    }

    @Override
    void onServiceDisconnected() {
        Switch sw =  findViewById(R.id.activity_setting_device_switch);
        sw.setOnCheckedChangeListener(null);
        sw.setEnabled(false);
        findViewById(R.id.activity_setting_btn).setEnabled(false);

        HOGPMessageService service = getHOGPMessageService();
        if (service != null) {
            service.getServiceProvider().removeServiceListener(this);
        }
    }

    @Override
    public void onServiceAdded(final DConnectService dConnectService) {
        updateHOGPService();
    }

    @Override
    public void onServiceRemoved(final DConnectService dConnectService) {
        updateHOGPService();
    }

    @Override
    public void onStatusChange(final DConnectService dConnectService) {
        if (mDeviceAdapter != null) {
            runOnUiThread(() -> {
                if (mDeviceAdapter != null) {
                    mDeviceAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    /**
     * HOGPサービスを更新します.
     */
    private void updateHOGPService() {
        HOGPMessageService service = getHOGPMessageService();
        if (service == null) {
            return;
        }

        final List<HOGPService> serviceList = new ArrayList<>();
        for (DConnectService s : service.getServiceProvider().getServiceList()) {
            if (s instanceof HOGPService) {
                serviceList.add((HOGPService) s);
            }
        }

        runOnUiThread(() -> {
            if (mDeviceAdapter != null) {
                mDeviceAdapter.setHOGPServiceList(serviceList);
            }
        });
    }

    /**
     * デバイス名をSwitchのタイトルに設定します.
     */
    private void setDeviceName() {
        TextView textView = findViewById(R.id.activity_setting_device_name);
        if (BleUtils.isBluetoothEnabled(this)) {
            String name = BleUtils.getBluetoothName(this);
            if (name != null) {
                textView.setText(name);
            }
        }
    }

    /**
     * HOGPサーバUIの設定を行います.
     */
    private void setHOGPServerUI() {
        final HOGPMessageService service = getHOGPMessageService();
        AbstractHOGPServer server = service.getHOGPServer();

        final Switch sw = findViewById(R.id.activity_setting_device_switch);
        sw.setChecked(server != null);
        sw.setEnabled(true);
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startHOGPServer();
            } else {
                service.stopHOGPServer();
                service.getHOGPSetting().setEnabledServer(false);
                setSwitchUI(false);
            }
        });

        findViewById(R.id.activity_setting_device_switch_title).setOnClickListener((v) -> {
            sw.setChecked(!sw.isChecked());
        });

        findViewById(R.id.activity_setting_hogp_keyboard_title).setOnClickListener((v) -> {
            Switch keyboard = findViewById(R.id.activity_setting_hogp_keyboard);
            keyboard.setChecked(!keyboard.isChecked());
        });

        Spinner mouse = findViewById(R.id.activity_setting_hogp_mouse_mode);
        mouse.setSelection(service.getHOGPSetting().getMouseMode().getValue());
        mouse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> adapter, final View v, final int position, final long id) {
                service.getHOGPSetting().setMouseMode(HOGPServer.MouseMode.valueOf(position));
            }

            @Override
            public void onNothingSelected(final AdapterView<?> adapter) {
            }
        });

        Switch keyboard =  findViewById(R.id.activity_setting_hogp_keyboard);
        keyboard.setChecked(service.getHOGPSetting().isEnabledKeyboard());
        keyboard.setOnCheckedChangeListener((buttonView, isChecked) -> {
            service.getHOGPSetting().setEnabledKeyboard(isChecked);
        });

        updateHOGPService();

        setEnabledSetting(isStartHOGPServer());
    }

    /**
     * ユーザ認可UIの設定を行います.
     */
    private void setLocalOAuthUI() {
        HOGPSetting setting = getHOGPMessageService().getHOGPSetting();
        final Switch oauthSwitch = findViewById(R.id.activity_setting_device_oauth_switch);
        oauthSwitch.setChecked(setting.isEnabledOAuth());
        oauthSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            HOGPMessageService service = getHOGPMessageService();
            if (service != null) {
                service.setEnabledOAuth(isChecked);
            }
        });

        findViewById(R.id.activity_setting_device_oauth_title).setOnClickListener((v) -> {
            oauthSwitch.setChecked(!oauthSwitch.isChecked());
        });
    }

    /**
     * HOGPサーバのOn/Offスイッチを設定します.
     * @param flag Onの場合はtrue、それ以外はfalse
     */
    private void setSwitchUI(final boolean flag) {
        ((Switch) findViewById(R.id.activity_setting_device_switch)).setChecked(flag);
        setEnabledSetting(flag);
    }

    /**
     * マウス・キーボード設定の有効・無効を設定します.
     * @param flag フラグ
     */
    private void setEnabledSetting(final boolean flag) {
        findViewById(R.id.activity_setting_hogp_mouse_mode).setEnabled(!flag);
        findViewById(R.id.activity_setting_hogp_keyboard).setEnabled(!flag);
        findViewById(R.id.activity_setting_hogp_keyboard_title).setEnabled(!flag);
    }

    /**
     * HOGPサーバが起動しているかを確認します.
     * @return HOGPサーバが起動している場合はtrue、それ以外はfalse
     */
    private boolean isStartHOGPServer() {
        HOGPMessageService service = getHOGPMessageService();
        if (service == null) {
            return false;
        }
        AbstractHOGPServer server = service.getHOGPServer();
        return server != null;
    }

    /**
     * 操作画面を開きます.
     */
    private void openControlActivity() {
        Intent intent = new Intent();
        intent.setClass(this, HOGPControlActivity.class);
        startActivity(intent);
    }

    /**
     * ヘルプ画面を開きます.
     */
    private void openHelpActivity() {
        Intent intent = new Intent();
        intent.setClass(this, HOGPHelpActivity.class);
        startActivity(intent);
    }

    /**
     * HOGPサーバを起動します.
     * <p>
     * HOGPサーバを起動するための設定やサポート状況の確認を行います。
     * </p>
     */
    private void startHOGPServer() {
        if (!BleUtils.isBluetoothEnabled(this)) {
            showDisableBluetooth();
            setSwitchUI(false);
        } else if (!BleUtils.isBleSupported(this)) {
            showNotSupportBLE();
            setSwitchUI(false);
        } else if (!BleUtils.isBlePeripheralSupported(this)) {
            showNotSupportPeripheral();
            setSwitchUI(false);
        } else if (!checkHOGPSetting()) {
            showNotSetFeature();
            setSwitchUI(false);
        } else {
            try {
                getHOGPMessageService().startHOGPServer();
                getHOGPMessageService().getHOGPSetting().setEnabledServer(true);
                setSwitchUI(true);
            } catch (Exception e) {
                showFailedStartServer();
                setSwitchUI(false);
            }
        }
    }

    /**
     * マウス・キーボードの設定状態を確認します.
     * @return 機能が設定されている場合はtrue、それ以外はfalse
     */
    private boolean checkHOGPSetting() {
        HOGPMessageService service = getHOGPMessageService();
        if (service == null) {
            return false;
        }
        HOGPSetting setting = service.getHOGPSetting();
        return (setting.getMouseMode() != HOGPServer.MouseMode.NONE || setting.isEnabledKeyboard());
    }

    /**
     * マウス・キーボードの機能が有効になっていないことを警告するダイアログを表示します.
     */
    private void showNotSetFeature() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_setting_hid_not_set_title)
                .setMessage(R.string.activity_setting_hid_not_set_message)
                .setPositiveButton(R.string.activity_setting_dialog_ok, null)
                .show();
    }

    /**
     * HOGPサーバの起動に失敗したことを警告するダイアログを表示します.
     */
    private void showFailedStartServer() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_setting_failed_to_start_server_title)
                .setMessage(R.string.activity_setting_failed_to_start_server_message)
                .setPositiveButton(R.string.activity_setting_dialog_ok, null)
                .show();
    }

    /**
     * Bluetooth設定が無効になっていることを警告するダイアログを表示します.
     */
    private void showDisableBluetooth() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_setting_bt_enabled_dialog_title)
                .setMessage(R.string.activity_setting_bt_enabled_dialog_message)
                .setPositiveButton(R.string.activity_setting_dialog_ok, (dialog, which) -> {
                    BleUtils.enableBluetooth(HOGPSettingActivity.this);
                })
                .setNegativeButton(R.string.activity_setting_dialog_no, null)
                .show();
    }

    /**
     * BLEがサポートされていないことを警告するダイアログを表示します.
     */
    private void showNotSupportBLE() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_setting_ble_not_support_title)
                .setMessage(R.string.activity_setting_ble_not_support_message)
                .setPositiveButton(R.string.activity_setting_dialog_ok, null)
                .show();
    }

    /**
     * Peripheralがサポートされていないことを警告するダイアログを表示します.
     */
    private void showNotSupportPeripheral() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_setting_peripheral_not_support_title)
                .setMessage(R.string.activity_setting_peripheral_not_support_message)
                .setPositiveButton(R.string.activity_setting_dialog_ok, null)
                .show();
    }

    /**
     * HOGPサーバが起動されていないことを警告するダイアログを表示します.
     */
    private void showNotStartServer() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_setting_not_start_server_title)
                .setMessage(R.string.activity_setting_not_start_server_message)
                .setPositiveButton(R.string.activity_setting_dialog_ok, null)
                .show();
    }

    /**
     * 接続されているデバイス一覧を表示するためのAdapter.
     */
    private class DeviceAdapter extends BaseAdapter {

        /**
         * HOGPServiceのリスト.
         */
        private List<HOGPService> mHOGPServiceList = new ArrayList<>();

        /**
         * サービスのリストを設定します.
         * @param serviceList サービスのリスト
         */
        void setHOGPServiceList(final List<HOGPService> serviceList) {
            mHOGPServiceList = serviceList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mHOGPServiceList.size();
        }

        @Override
        public Object getItem(final int position) {
            return mHOGPServiceList.get(position);
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_setting_device, null);
            }

            TextView nameView = convertView.findViewById(R.id.activity_setting_device_name);
            TextView statusView = convertView.findViewById(R.id.activity_setting_device_status);

            HOGPService service = (HOGPService) getItem(position);
            nameView.setText(service.getName());

            String status = service.isOnline() ? getString(R.string.activity_setting_device_online) :
                    getString(R.string.activity_setting_device_offline);

            statusView.setText(status);

            return convertView;
        }
    }
}
