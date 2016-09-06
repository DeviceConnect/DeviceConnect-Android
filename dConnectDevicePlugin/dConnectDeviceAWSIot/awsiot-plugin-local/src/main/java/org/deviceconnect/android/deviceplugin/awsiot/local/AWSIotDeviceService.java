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

import com.amazonaws.regions.Regions;

import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotPrefUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class AWSIotDeviceService extends Service {

    public static final String ACTION_START = "org.deviceconnect.android.deviceplugin.awsiot.local.ACTION_START";
    public static final String ACTION_STOP = "org.deviceconnect.android.deviceplugin.awsiot.local.ACTION_STOP";

    private AWSIotLocalManager mAWSIoTLocalManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
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
        DConnectHelper.INSTANCE.sendRequest("GET", "http://localhost:4035/gotapi/availability", callback);
    }

    private void startAWSIot() {
        availability(new DConnectHelper.FinishCallback() {
            @Override
            public void onFinish(final String response, final Exception error) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int result = jsonObject.getInt("result");
                    if (result == 0) {
                        String name = jsonObject.getString("name");
                        String uuid = jsonObject.getString("uuid");
                        startAWSIot(name, uuid);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void startAWSIot(final String name, final String uuid) {
        // TODO
        AWSIotPrefUtil pref = new AWSIotPrefUtil(this);

        mAWSIoTLocalManager = new AWSIotLocalManager(this, name, uuid);
        mAWSIoTLocalManager.connectAWSIoT(pref.getAccessKey(),
                pref.getSecretKey(), pref.getRegions());
    }

    private void stopAWSIot() {
        if (mAWSIoTLocalManager != null) {
            mAWSIoTLocalManager.disconnect();
            mAWSIoTLocalManager = null;
        }
    }
}