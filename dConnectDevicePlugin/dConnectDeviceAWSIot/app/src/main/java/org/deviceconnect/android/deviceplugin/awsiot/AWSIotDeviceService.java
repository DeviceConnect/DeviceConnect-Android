/*
 AWSIotDeviceService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot;

import com.amazonaws.regions.Regions;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotController;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotDeviceApplication;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotPrefUtil;
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

        // ログインフラグがtrueの場合には自動接続を行う
        AWSIotPrefUtil pref = new AWSIotPrefUtil(this);
        if (pref.isAWSLoginFlag()) {
            String accessKey = pref.getAccessKey();
            String secretKey = pref.getSecretKey();
            Regions region = pref.getRegions();
            getAWSIotController().connect(accessKey, secretKey, region, new AWSIotController.ConnectCallback() {
                @Override
                public void onConnected(final Exception err) {
                    if (err == null) {
                        ((AWSIotDeviceApplication) getApplication()).getRDCMListManager().subscribeShadow();
                        AWSIotDeviceApplication.getInstance().updateMyManagerShadow(true);
                        AWSIotPrefUtil pref = new AWSIotPrefUtil(getContext());
                        if (pref.getManagerRegister()) {
                            Intent intent = new Intent();
                            intent.setClass(getApplicationContext(), AWSIotLocalDeviceService.class);
                            intent.setAction(AWSIotLocalDeviceService.ACTION_START);
                            getApplicationContext().startService(intent);
                        }
                    }
                }
            });
        }
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