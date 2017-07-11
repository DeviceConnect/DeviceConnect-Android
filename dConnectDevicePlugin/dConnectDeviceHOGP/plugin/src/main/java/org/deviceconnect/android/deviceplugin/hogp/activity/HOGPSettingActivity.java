package org.deviceconnect.android.deviceplugin.hogp.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.deviceconnect.android.deviceplugin.hogp.HOGPMessageService;
import org.deviceconnect.android.deviceplugin.hogp.R;
import org.deviceconnect.android.deviceplugin.hogp.server.HOGPServer;
import org.deviceconnect.android.deviceplugin.hogp.util.BleUtils;


public class HOGPSettingActivity extends HOGPBaseActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Intent intent = new Intent();
        intent.setClass(this, HOGPMessageService.class);
        startService(intent);

        findViewById(R.id.activity_setting_device_switch).setEnabled(false);
        findViewById(R.id.activity_setting_btn).setEnabled(false);
        findViewById(R.id.activity_setting_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (isStartHOGPServer()) {
                    openControlActivity();
                } else {
                    showNotStartServer();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BleUtils.REQUEST_CODE_BLUETOOTH_ENABLE) {
            if (resultCode == RESULT_OK) {
                startHOGPServer();
            }
        }
    }

    @Override
    void onServiceConnected() {
        HOGPMessageService service = getHOGPMessageService();
        HOGPServer server = service.getHOGPServer();

        final Switch sw = (Switch) findViewById(R.id.activity_setting_device_switch);
        sw.setChecked(server != null);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                if (isChecked) {
                    startHOGPServer();
                } else {
                    getHOGPMessageService().stopHOGPServer();
                    setSwitchUI(false);
                }
            }
        });
        sw.setEnabled(true);

        findViewById(R.id.activity_setting_btn).setEnabled(true);
    }

    @Override
    void onServiceDisconnected() {
        Switch sw = (Switch) findViewById(R.id.activity_setting_device_switch);
        sw.setOnCheckedChangeListener(null);
        sw.setEnabled(false);
        findViewById(R.id.activity_setting_btn).setEnabled(false);
    }

    private void setSwitchUI(final boolean flag) {
        ((Switch) findViewById(R.id.activity_setting_device_switch)).setChecked(flag);
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
        HOGPServer server = service.getHOGPServer();
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
        } else {
            try {
                getHOGPMessageService().startHOGPServer();
                setSwitchUI(true);
            } catch (Exception e) {
                showFailedStartServer();
                setSwitchUI(false);
            }
        }
    }

    /**
     * HOGPサーバの軌道に失敗したことを警告するダイアログを表示します.
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
                .setPositiveButton(R.string.activity_setting_dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        BleUtils.enableBluetooth(HOGPSettingActivity.this);
                    }
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
}
