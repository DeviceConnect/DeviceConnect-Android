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

import org.deviceconnect.android.deviceplugin.awsiot.AWSIotDeviceService;
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

        loginAWSIot();
    }

    @Override
    public void onTerminate() {
        if (mRDCMListManager != null) {
            mRDCMListManager.stopUpdateManagerListTimer();
        }
        logoutAWSIot();
        super.onTerminate();
    }

    public void updateMyManagerShadow(final boolean online) {
        updateMyManagerShadow(online, new AWSIotController.UpdateShadowCallback() {
            @Override
            public void onUpdateShadow(final String result, final Exception err) {
            }
        });
    }

    public void updateMyManagerShadow(final boolean online, final AWSIotController.UpdateShadowCallback callback) {
        if (mIot.isLogin()) {
            try {
                AWSIotPrefUtil prefUtil = new AWSIotPrefUtil(this);
                if (prefUtil.getManagerName() == null) {
                    return;
                }

                JSONObject managerData = new JSONObject();
                managerData.put("name", prefUtil.getManagerName());
                managerData.put("online", online);
                managerData.put("timeStamp", System.currentTimeMillis());

                mIot.updateShadow(AWSIotUtil.KEY_DCONNECT_SHADOW_NAME, prefUtil.getManagerUuid(), managerData, callback);
            } catch (JSONException e) {
                if (callback != null) {
                    callback.onUpdateShadow(null, e);
                }
            }
        } else {
            if (callback != null) {
                callback.onUpdateShadow(null, new Exception("Not login."));
            }
        }
    }

    public void loginAWSIot() {
        AWSIotPrefUtil pref = new AWSIotPrefUtil(this);
        if (pref.isAWSLoginFlag()) {
            String accessKey = pref.getAccessKey();
            String secretKey = pref.getSecretKey();
            Regions region = pref.getRegions();
            mIot.login(accessKey, secretKey, region, new AWSIotController.LoginCallback() {
                @Override
                public void onLogin(final Exception err) {
                    if (err == null) {
                        startAWSIot();
                    }
                }
            });
        }
    }

    public void logoutAWSIot() {
        AWSIotPrefUtil pref = new AWSIotPrefUtil(this);
        pref.setAWSLoginFlag(false);
        updateMyManagerShadow(false, new AWSIotController.UpdateShadowCallback() {
            @Override
            public void onUpdateShadow(final String result, final Exception err) {
                mIot.logout();
            }
        });
    }

    public void startAWSIot() {
        Intent intent = new Intent();
        intent.setClass(this, AWSIotDeviceService.class);
        intent.setAction(AWSIotDeviceService.ACTION_CONNECT_MQTT);
        startService(intent);

        AWSIotPrefUtil pref = new AWSIotPrefUtil(this);
        if (pref.getManagerRegister()) {
            updateMyManagerShadow(true);

            Intent intent2 = new Intent();
            intent2.setClass(this, AWSIotLocalDeviceService.class);
            intent2.setAction(AWSIotLocalDeviceService.ACTION_START);
            startService(intent2);
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
