/*
 GeolocationAlertDialogActivity.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.message.MessageUtils;

/**
 * Geolocation Alert Dialog Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class GeolocationAlertDialogActivity extends AppCompatActivity {
    private Activity mActivity;
    private Intent mResponse;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("Intent");
        if (bundle != null) {
            mResponse = bundle.getParcelable("response");
        }
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        if (getActionBar() != null) {
            getActionBar().hide();
        }

        setContentView(R.layout.activity_geolocation_alert_dialog);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.gps_settings_title);
        alertDialogBuilder.setMessage(R.string.host_setting_dialog_disable_gps)
                .setCancelable(false)

                // GPS設定画面起動用ボタンとイベント定義
                .setPositiveButton(R.string.gps_settings_title,
                        (dialog, id) -> {
                            Intent callGPSSettingIntent = new Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            try {
                                startActivity(callGPSSettingIntent);
                                if (mResponse != null) {
                                    MessageUtils.setIllegalDeviceStateError(mResponse,
                                            "Please retry running Geolocation.");
                                    getBaseContext().sendBroadcast(mResponse);
                                }

                            } catch (ActivityNotFoundException e) {
                                if (mResponse != null) {
                                    MessageUtils.setIllegalDeviceStateError(mResponse,
                                            "GPS setting is not enabled.");
                                    getBaseContext().sendBroadcast(mResponse);
                                }
                            }
                            mActivity.finish();
                        });
        // キャンセルボタン処理
        alertDialogBuilder.setNegativeButton(R.string.host_setting_gps_dialog_cancel,
                (dialog, id) -> {
                    if (mResponse != null) {
                        MessageUtils.setIllegalDeviceStateError(mResponse,
                                "GPS setting is not enabled.");
                        getBaseContext().sendBroadcast(mResponse);
                    }
                    dialog.cancel();
                    mActivity.finish();
                });
        AlertDialog alert = alertDialogBuilder.create();
        // 設定画面移動問い合わせダイアログ表示
        alert.show();
    }
}
