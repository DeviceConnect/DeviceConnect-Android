/*
 OverlayPermissionActivity.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.protection;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.Nullable;

/**
 * オーバーレイの許可を取得する画面.
 *
 * @author NTT DOCOMO, INC.
 */
@TargetApi(Build.VERSION_CODES.M)
public class OverlayPermissionActivity extends Activity {
    /**
     * オーバーレイ許可用のリクエストコードを定義.
     */
    private static final int REQUEST_CODE_OVERLAY = 1234;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPermissionActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_OVERLAY) {
            if (Settings.canDrawOverlays(getApplicationContext())) {
                Intent broadcast = new Intent(ScreenRecordingGuardOverlay.ACTION_PERMISSION_RESULT);
                sendBroadcast(broadcast);
            }
        }

        finish();
    }

    private void startPermissionActivity() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CODE_OVERLAY);
    }
}
