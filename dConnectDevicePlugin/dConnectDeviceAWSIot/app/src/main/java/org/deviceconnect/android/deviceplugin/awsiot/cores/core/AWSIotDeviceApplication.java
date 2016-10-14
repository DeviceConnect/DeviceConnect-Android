/*
 AWSIotDeviceApplication.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.cores.core;

import android.app.Application;
import android.content.Intent;

import com.amazonaws.regions.Regions;

import org.deviceconnect.android.deviceplugin.awsiot.cores.util.AWSIotUtil;
import org.deviceconnect.android.deviceplugin.awsiot.local.AWSIotLocalDeviceService;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * AWS IoT Device Plugin Application.
 *
 * @author NTT DOCOMO, INC.
 */
public class AWSIotDeviceApplication extends Application {
    /** Singleton Instance. */
    private static AWSIotDeviceApplication sInstance;
    /** AWSIotコントローラー */
    private final AWSIotController mIot = new AWSIotController();
    /** Instance of {@link RDCMListManager}. */
    private RDCMListManager mRDCMListManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        mRDCMListManager = new RDCMListManager(getApplicationContext(), mIot);
        mRDCMListManager.startUpdateManagerListTimer();
        mRDCMListManager.subscribeShadow();

        // ログインフラグがtrueの場合には自動接続を行う
        final AWSIotPrefUtil pref = new AWSIotPrefUtil(this);
        if (pref.isAWSLoginFlag()) {
            String accessKey = pref.getAccessKey();
            String secretKey = pref.getSecretKey();
            Regions region = pref.getRegions();
            mIot.connect(accessKey, secretKey, region, new AWSIotController.ConnectCallback() {
                @Override
                public void onConnected(final Exception err) {
                    if (err == null) {
                        mRDCMListManager.subscribeShadow();
                        mRDCMListManager.updateManagerList(null);
                        updateMyManagerShadow(true);
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
    public void onTerminate() {
        if (mRDCMListManager != null) {
            mRDCMListManager.unsubscribeShadow();
            mRDCMListManager.stopUpdateManagerListTimer();
        }
        super.onTerminate();
    }

    public void updateMyManagerShadow(boolean online) {
        if (mIot.isConnected()) {
            try {
                AWSIotPrefUtil prefUtil = new AWSIotPrefUtil(this);
                if (prefUtil.getManagerName() == null) {
                    return;
                }

                JSONObject managerData = new JSONObject();
                managerData.put("name", prefUtil.getManagerName());
                managerData.put("online", online);
                managerData.put("timeStamp", System.currentTimeMillis());

                mIot.updateShadow(AWSIotUtil.KEY_DCONNECT_SHADOW_NAME, prefUtil.getManagerUuid(), managerData, new AWSIotController.UpdateShadowCallback() {
                    @Override
                    public void onUpdateShadow(final String result, final Exception err) {
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public AWSIotController getAWSIotController() {
        return mIot;
    }

    public RDCMListManager getRDCMListManager() {
        return mRDCMListManager;
    }

    public static synchronized AWSIotDeviceApplication getInstance() {
        return sInstance;
    }
}
