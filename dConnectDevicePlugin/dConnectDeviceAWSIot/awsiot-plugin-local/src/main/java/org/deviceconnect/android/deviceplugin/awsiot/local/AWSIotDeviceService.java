/*
 AWSIotDeviceService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.local;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotPrefUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class AWSIotDeviceService extends Service {

    private static final String TAG = "AWS-Local";

    public static final String ACTION_START = "org.deviceconnect.android.deviceplugin.awsiot.local.ACTION_START";
    public static final String ACTION_STOP = "org.deviceconnect.android.deviceplugin.awsiot.local.ACTION_STOP";

    private AWSIotLocalManager mAWSIoTLocalManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        android.os.Debug.waitForDebugger();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        stopAWSIot();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                startAWSIot();
            } else if (ACTION_STOP.equals(action)) {
                stopAWSIot();
                stopSelf();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void availability(final DConnectHelper.FinishCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DConnectHelper.INSTANCE.sendRequest("GET", "http://localhost:4035/gotapi/availability", callback);
            }
        }).start();
    }

    private void startAWSIot() {
        availability(new DConnectHelper.FinishCallback() {
            @Override
            public void onFinish(final String response, final Exception error) {
                if (response == null) {
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int result = jsonObject.getInt("result");
                    if (result == 0) {
                        AWSIotPrefUtil pref = new AWSIotPrefUtil(AWSIotDeviceService.this);
                        String name = jsonObject.optString("name");
                        String uuid = jsonObject.optString("uuid");
                        if (name == null || uuid == null) {
                            // TODO 古いManager場合の処理
                            String prefName = pref.getManagerName();
                            name = "TEST";
                            if (prefName.matches(name)) {
                                uuid = pref.getManagerUuid();
                            }
                            if (uuid == null) {
                                uuid = UUID.randomUUID().toString();
                            }
                        }
                        pref.setManagerName(name);
                        pref.setManagerUuid(uuid);

                        startAWSIot(name, uuid);
                    } else {
                        // TODO Managerが起動していない場合の処理
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "", e);
                }
            }
        });
    }

    private synchronized void startAWSIot(final String name, final String uuid) {
        if (mAWSIoTLocalManager != null) {
            mAWSIoTLocalManager.disconnect();
        }

        AWSIotPrefUtil pref = new AWSIotPrefUtil(this);
        mAWSIoTLocalManager = new AWSIotLocalManager(this, name, uuid);
        mAWSIoTLocalManager.connectAWSIoT(pref.getAccessKey(),
                pref.getSecretKey(), pref.getRegions());
    }

    private synchronized void stopAWSIot() {
        if (mAWSIoTLocalManager != null) {
            mAWSIoTLocalManager.disconnect();
            mAWSIoTLocalManager = null;
        }
    }
}