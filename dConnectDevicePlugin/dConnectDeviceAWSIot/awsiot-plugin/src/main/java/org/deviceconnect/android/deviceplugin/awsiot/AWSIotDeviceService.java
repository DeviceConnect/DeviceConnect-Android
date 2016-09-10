/*
 AWSIotDeviceService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotController;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotDeviceApplication;
import org.deviceconnect.android.deviceplugin.awsiot.profile.AWSIotServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.awsiot.profile.AWSIotSystemProfile;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * 本デバイスプラグインのプロファイルをDeviceConnectに登録するサービス.
 * @author NTT DOCOMO, INC.
 */
public class AWSIotDeviceService extends DConnectMessageService {
    private static final String TAG = "AWS-Remote";

    public static final String ACTION_CONNECT_MQTT = "org.deviceconnect.android.deviceplugin.awsiot.ACTION_CONNECT_MQTT";

    private AWSIotRemoteManager mAWSIotRemoteManager;

    @Override
    public void onCreate() {
//        android.os.Debug.waitForDebugger();
        super.onCreate();

        EventManager.INSTANCE.setController(new MemoryCacheController());
        startAWSIot();
        addProfile(new AWSIotServiceDiscoveryProfile(mAWSIotRemoteManager, getServiceProvider()));
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
            if (ACTION_CONNECT_MQTT.equals(action)) {
                mAWSIotRemoteManager.connect();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new AWSIotSystemProfile();
    }

    @Override
    protected void onManagerUninstalled() {
        stopAWSIot();
    }

    @Override
    protected void onDevicePluginReset() {
        stopAWSIot();
        startAWSIot();
    }

    @Override
    protected boolean executeRequest(final String profileName, final Intent request, final Intent response) {
        DConnectProfile profile = getProfile(profileName);
        if (profile == null) {
            return mAWSIotRemoteManager.sendRequest(request, response);
        } else {
            return profile.onRequest(request, response);
        }
    }

    private void startAWSIot() {
        mAWSIotRemoteManager = new AWSIotRemoteManager(this, getAWSIotController());
        mAWSIotRemoteManager.connect();
    }

    private void stopAWSIot() {
        if (mAWSIotRemoteManager != null) {
            mAWSIotRemoteManager.disconnect();
            mAWSIotRemoteManager = null;
        }
    }

    private AWSIotController getAWSIotController() {
        return ((AWSIotDeviceApplication) getApplication()).getAWSIotController();
    }
}