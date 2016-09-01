/*
 AWSIotDeviceService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotPrefUtil;
import org.deviceconnect.android.deviceplugin.awsiot.profile.AWSIotSystemProfile;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * 本デバイスプラグインのプロファイルをDeviceConnectに登録するサービス.
 * @author NTT DOCOMO, INC.
 */
public class AWSIotDeviceService extends DConnectMessageService {
    private AWSIotRemoteManager mAWSIotRemoteManager;
    private AWSIotPrefUtil mAWSIotPrefUtil;

    @Override
    public void onCreate() {
        super.onCreate();

        setUseLocalOAuth(false);

        EventManager.INSTANCE.setController(new MemoryCacheController());

        mAWSIotPrefUtil = new AWSIotPrefUtil(this);

        // TODO 開始タイミングを検討すること
        startAWSIot();
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
    protected boolean executeRequest(final String profileName, final Intent request, final Intent response) {
        return mAWSIotRemoteManager.sendRequestToRemoteDeviceConnectManager(request, response);
    }

    private void startAWSIot() {
        mAWSIotRemoteManager = new AWSIotRemoteManager(this);
        mAWSIotRemoteManager.connectAWSIoT(mAWSIotPrefUtil.getAccessKey(),
                mAWSIotPrefUtil.getSecretKey(), mAWSIotPrefUtil.getRegions());
    }

    private void stopAWSIot() {
        if (mAWSIotRemoteManager != null) {
            mAWSIotRemoteManager.destroy();
        }
    }
}