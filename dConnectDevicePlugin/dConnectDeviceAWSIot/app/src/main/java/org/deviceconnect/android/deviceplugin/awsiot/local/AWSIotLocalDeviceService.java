/*
 AWSIotLocalDeviceService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.local;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotController;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotDeviceApplication;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotPrefUtil;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.RemoteDeviceConnectManager;
import org.deviceconnect.android.deviceplugin.awsiot.remote.BuildConfig;

public class AWSIotLocalDeviceService extends Service {

    private static final boolean DEBUG = BuildConfig.DEBUG;
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

    private void startAWSIot() {
        stopAWSIot();

        if (DEBUG) {
            Log.i(TAG, "@@@@@@@ AWSIotDeviceService#startAWSIot()");
        }

        AWSIotPrefUtil pref = new AWSIotPrefUtil(this);
        RemoteDeviceConnectManager remote = new RemoteDeviceConnectManager(pref.getManagerName(), pref.getManagerUuid());
        mAWSIoTLocalManager = new AWSIotLocalManager(this, getAWSIotController(), remote);
        mAWSIoTLocalManager.connectAWSIoT();
    }

    private void stopAWSIot() {
        if (DEBUG) {
            Log.i(TAG, "@@@@@@@ AWSIotDeviceService#stopAWSIot()");
        }

        if (mAWSIoTLocalManager != null) {
            mAWSIoTLocalManager.disconnectAWSIoT();
            mAWSIoTLocalManager = null;
        }
    }

    private AWSIotController getAWSIotController() {
        return ((AWSIotDeviceApplication) getApplication()).getAWSIotController();
    }
}