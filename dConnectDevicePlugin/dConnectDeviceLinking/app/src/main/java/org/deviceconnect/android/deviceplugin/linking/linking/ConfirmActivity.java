/*
 ConfirmActivity.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.R;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ConfirmActivity extends Activity {

    private static final String TAG = "ABC";

    private static final int REQUEST_CODE = 4;

    private int mCurrentRequestType = 0;

    private ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mScheduledFuture;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "ConfirmActivity:onCreate");
        }
        setContentView(R.layout.activity_confirm);

        mScheduledFuture = mExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "ConfirmActivity timeout.");
                }
                finish();
            }
        }, 30, TimeUnit.SECONDS);

        startSensor(mCurrentRequestType);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "ConfirmActivity:onActivityResult");
            Log.i(TAG, "requestCode:" + requestCode);
            Log.i(TAG, "resultCode:" + resultCode);
            Log.i(TAG, "mCurrentRequestType:" + mCurrentRequestType);
        }

        if (requestCode != REQUEST_CODE) {
            finishConfirmActivity();
            return;
        }
        if (resultCode != LinkingUtil.RESULT_OK &&
                resultCode != LinkingUtil.RESULT_SENSOR_UNSUPPORTED) {
            finishConfirmActivity();
            return;
        }
        if (mCurrentRequestType == 2) {
            finishConfirmActivity();
            return;
        }
        mCurrentRequestType += 1;
        startSensor(mCurrentRequestType);
    }

    private void finishConfirmActivity() {
        if (mScheduledFuture != null) {
            mScheduledFuture.cancel(true);
        }
        finish();
    }

    private void startSensor(final int type) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "ConfirmActivity:startSensor type:" + type);
        }

        Intent intent = new Intent("com.nttdocomo.android.smartdeviceagent.action.START_SENSOR");
        intent.setComponent(new ComponentName("com.nttdocomo.android.smartdeviceagent",
                "com.nttdocomo.android.smartdeviceagent.RequestStartActivity"));
        intent.putExtras(getIntent().getExtras());
        intent.putExtra("com.nttdocomo.android.smartdeviceagent.extra.SENSOR_TYPE", type);
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            finishConfirmActivity();
        }
    }
}
