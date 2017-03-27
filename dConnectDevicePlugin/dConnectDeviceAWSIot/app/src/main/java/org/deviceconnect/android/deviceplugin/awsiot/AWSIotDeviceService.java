/*
 AWSIotDeviceService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotController;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotDeviceApplication;
import org.deviceconnect.android.deviceplugin.awsiot.local.AWSIotLocalDeviceService;
import org.deviceconnect.android.deviceplugin.awsiot.profile.AWSIotServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.awsiot.profile.AWSIotSystemProfile;
import org.deviceconnect.android.deviceplugin.awsiot.remote.AWSIotRemoteManager;
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
    public static final String ACTION_CONNECT_MQTT = "org.deviceconnect.android.deviceplugin.awsiot.ACTION_CONNECT_MQTT";

    private AWSIotRemoteManager mAWSIotRemoteManager;

    @Override
    public void onCreate() {
        super.onCreate();

        setUseLocalOAuth(false);

        EventManager.INSTANCE.setController(new MemoryCacheController());
        startRemoteAWSIot();
        addProfile(new AWSIotServiceDiscoveryProfile(this, getServiceProvider()));
    }

    @Override
    public void onDestroy() {
        stopRemoteAWSIot();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_CONNECT_MQTT.equals(action)) {
                if (mAWSIotRemoteManager != null) {
                    mAWSIotRemoteManager.connect();
                }
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
        stopRemoteAWSIot();
    }

    @Override
    protected void onDevicePluginReset() {
        stopRemoteAWSIot();
        stopLocalAWSIot();

        getAWSIotController().logout();

        startRemoteAWSIot();
        startLocalAWSIot();

        ((AWSIotDeviceApplication) getApplication()).loginAWSIot();
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

    public AWSIotRemoteManager getAWSIotRemoteManager() {
        return mAWSIotRemoteManager;
    }

    private void startRemoteAWSIot() {
        mAWSIotRemoteManager = new AWSIotRemoteManager(this, getAWSIotController());
        mAWSIotRemoteManager.connect();
    }

    private void stopRemoteAWSIot() {
        if (mAWSIotRemoteManager != null) {
            mAWSIotRemoteManager.disconnect();
            mAWSIotRemoteManager = null;
        }
    }

    private void startLocalAWSIot() {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), AWSIotLocalDeviceService.class);
        intent.setAction(AWSIotLocalDeviceService.ACTION_START);
        getApplicationContext().startService(intent);
    }

    private void stopLocalAWSIot() {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), AWSIotLocalDeviceService.class);
        intent.setAction(AWSIotLocalDeviceService.ACTION_STOP);
        getApplicationContext().startService(intent);
    }

    private AWSIotController getAWSIotController() {
        return ((AWSIotDeviceApplication) getApplication()).getAWSIotController();
    }
}