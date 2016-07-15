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
import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.R;

public class ConfirmActivity extends Activity {

    private static final String TAG = "ConfirmActivity";

    private static final int REQUEST_CODE = 4;

    public static final String EXTRA_REQUEST_SENSOR_TYPE = "extra_request_sensor_type";

    /**
     * 0：ジャイロセンサー
     * 1：加速度センサー
     * 2：方位センサー
     * 3: 電池残量
     * 4: 温度センサー
     * 5: 湿度センサー
     * 6～255：拡張センサー
     */
    private int[] mRequestType;

    private int mIndex;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linking_confirm);

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "ConfirmActivity:onCreate");
        }

        Intent intent = getIntent();
        if (intent != null) {
            mRequestType = intent.getIntArrayExtra(EXTRA_REQUEST_SENSOR_TYPE);
            if (BuildConfig.DEBUG) {
                if (mRequestType != null) {
                    for (int type : mRequestType) {
                        Log.d(TAG, "RequestType: " + type);
                    }
                }
            }
        }

        if (mRequestType == null || mRequestType.length == 0) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "RequestType is null.");
            }
            finishConfirmActivity();
        } else {
            startSensor();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        LinkingUtil.Result result = LinkingUtil.Result.valueOf(resultCode);

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "ConfirmActivity:onActivityResult");
            Log.i(TAG, "requestCode: " + requestCode);
            Log.i(TAG, "resultCode: " + result);
            Log.i(TAG, "mIndex: " + mIndex);
        }

        if (requestCode != REQUEST_CODE) {
            finishConfirmActivity();
            return;
        }
        if (result != LinkingUtil.Result.RESULT_OK &&
                result != LinkingUtil.Result.RESULT_SENSOR_UNSUPPORTED) {
            finishConfirmActivity();
            return;
        }
        if (mRequestType.length <= mIndex) {
            finishConfirmActivity();
            return;
        }
        startSensor();
    }

    private void finishConfirmActivity() {

        Intent intent = getIntent();
        if (intent != null) {
            LinkingDeviceManager mgr = getLinkingDeviceManager();
            if (mgr != null) {
                mgr.onConfirmActivityResult(intent);
            }
        }

        finish();
    }

    private void startSensor() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "ConfirmActivity:startSensor type:" + mRequestType[mIndex]);
        }

        Intent intent = new Intent(LinkingUtil.ACTION_START_SENSOR);
        intent.setComponent(new ComponentName(LinkingUtil.PACKAGE_NAME, LinkingUtil.ACTIVITY_NAME));
        intent.putExtras(getIntent().getExtras());
        intent.putExtra(LinkingUtil.EXTRA_SENSOR_TYPE, mRequestType[mIndex]);
        mIndex++;
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            finishConfirmActivity();
        }
    }

    private LinkingDeviceManager getLinkingDeviceManager() {
        LinkingApplication app = (LinkingApplication) getApplication();
        return app.getLinkingDeviceManager();
    }
}
