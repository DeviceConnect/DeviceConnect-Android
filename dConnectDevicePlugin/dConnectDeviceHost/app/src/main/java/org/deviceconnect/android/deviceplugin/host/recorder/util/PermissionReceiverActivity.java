/*
 PermissionReceiverActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * スクリーンキャプチャの許可を受け取るための Activity.
 *
 * @author NTT DOCOMO, INC.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PermissionReceiverActivity extends Activity {

    static final String RESULT_DATA = "result_data";

    static final String EXTRA_CALLBACK = "callback";

    private static final int REQUEST_CODE = 1234567;

    private MediaProjectionManager mManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // ステータスバーを消す
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );

        mManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // スクリーンのキャプチャ許可を行う画面を呼び出します。
        Intent intent = mManager.createScreenCaptureIntent();
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode != REQUEST_CODE) {
            return;
        }

        Bundle response = new Bundle();
        response.putParcelable(RESULT_DATA, data);

        ResultReceiver callback = getIntent().getParcelableExtra(EXTRA_CALLBACK);
        if (callback != null) {
            callback.send(Activity.RESULT_OK, response);
        }
        finish();
    }
}
