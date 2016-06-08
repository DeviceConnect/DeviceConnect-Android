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

public class ConfirmActivity extends Activity {

    private static final String TAG = "LinkingPlugIn";
    private int mCurrentRequestType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "ConfirmActivity:onCreate");
        }
        setContentView(R.layout.activity_confirm);
        startSensor(mCurrentRequestType);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "ConfirmActivity:onActivityResult");
            Log.i(TAG, "requestCode:" + requestCode);
            Log.i(TAG, "resultCode:" + resultCode);
            Log.i(TAG, "mCurrentRequestType:" + mCurrentRequestType);
        }

        if (requestCode != LinkingUtil.RESULT_DEVICE_OFF) {
            finish();
            return;
        }
        if (resultCode != LinkingUtil.RESULT_OK &&
                resultCode != LinkingUtil.RESULT_SENSOR_UNSUPPORTED) {
            finish();
            return;
        }
        if (mCurrentRequestType == 2) {
            finish();
            return;
        }
        mCurrentRequestType += 1;
        startSensor(mCurrentRequestType);
    }

    private void startSensor(int type) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "ConfirmActivity:startSensor type:" + type);
        }
        Intent intent = new Intent("com.nttdocomo.android.smartdeviceagent.action.START_SENSOR");
        intent.setComponent(new ComponentName("com.nttdocomo.android.smartdeviceagent", "com.nttdocomo.android.smartdeviceagent.RequestStartActivity"));
        intent.putExtras(getIntent().getExtras());
        intent.putExtra("com.nttdocomo.android.smartdeviceagent.extra.SENSOR_TYPE", type);
        try {
            startActivityForResult(intent, 4);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            finish();
        }
    }
}
