/*
 AWSIotDeviceService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotPrefUtil;
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
    private AWSIotRemoteManager mAWSIotRemoteManager;

    @Override
    public void onCreate() {
        super.onCreate();

        EventManager.INSTANCE.setController(new MemoryCacheController());

        // TODO 開始タイミングを検討すること
        startAWSIot();

        addProfile(new AWSIotServiceDiscoveryProfile(mAWSIotRemoteManager, getServiceProvider()));
    }

    @Override
    public void onDestroy() {
        stopAWSIot();
        super.onDestroy();
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
        if (mAWSIotRemoteManager != null) {
            mAWSIotRemoteManager.disconnect();
        }

        AWSIotPrefUtil pref = new AWSIotPrefUtil(this);
        mAWSIotRemoteManager = new AWSIotRemoteManager(this);
        mAWSIotRemoteManager.connectAWSIoT(pref.getAccessKey(),
                pref.getSecretKey(), pref.getRegions());
    }

    private void stopAWSIot() {
        if (mAWSIotRemoteManager != null) {
            mAWSIotRemoteManager.disconnect();
        }
    }
}