/*
 HvcLocationAlertDialogActivity.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import org.deviceconnect.android.deviceplugin.hvc.R;

/**
 * HVC Location Alert Dialog Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class HvcLocationAlertDialog extends Activity {
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        setContentView(R.layout.hvc_location_alert_dialog);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.location_settings_title);
        alertDialogBuilder.setMessage(R.string.setting_dialog_disable_location)
                .setCancelable(false)

                // 位置情報設定画面起動用ボタンとイベント定義
                .setPositiveButton(R.string.location_settings_title,
                        (dialog, id) -> {
                            Intent callLocationSettingIntent = new Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(callLocationSettingIntent);
                            mActivity.finish();
                        });
        // キャンセルボタン処理
        alertDialogBuilder.setNegativeButton(R.string.setting_location_dialog_cancel,
                (dialog, id) -> {
                    dialog.cancel();
                    mActivity.finish();
                });
        AlertDialog alert = alertDialogBuilder.create();
        // 設定画面移動問い合わせダイアログ表示
        alert.show();
    }
}
