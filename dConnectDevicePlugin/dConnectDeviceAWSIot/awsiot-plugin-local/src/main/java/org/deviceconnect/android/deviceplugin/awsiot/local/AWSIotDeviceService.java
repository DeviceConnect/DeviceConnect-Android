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
import android.support.annotation.Nullable;

import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotPrefUtil;

public class AWSIotDeviceService extends Service {

    private AWSIotLocalManager mAWSIoTLocalManager;
    private AWSIotPrefUtil mAWSIotPrefUtil;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mAWSIotPrefUtil = new AWSIotPrefUtil(this);

        // TODO AWSIotの開始タイミングを検討すること。
        startAWSIot();
    }

    @Override
    public void onDestroy() {
        stopAWSIot();
        super.onDestroy();
    }

    private String availability() {
        // TODO Device Connect Managerから名前を取得すること。
        return "{}";
    }

    private void startAWSIot() {
        String name = availability();
        if (name == null) {
            // TODO 名前が取得できない場合の処理
            return;
        }

        mAWSIoTLocalManager = new AWSIotLocalManager(this, "nobu", "test");
        mAWSIoTLocalManager.connectAWSIoT(mAWSIotPrefUtil.getAccessKey(),
                mAWSIotPrefUtil.getSecretKey(), mAWSIotPrefUtil.getRegions());
    }

    private void stopAWSIot() {
        if (mAWSIoTLocalManager != null) {
            mAWSIoTLocalManager.destroy();
            mAWSIoTLocalManager = null;
        }
    }
}