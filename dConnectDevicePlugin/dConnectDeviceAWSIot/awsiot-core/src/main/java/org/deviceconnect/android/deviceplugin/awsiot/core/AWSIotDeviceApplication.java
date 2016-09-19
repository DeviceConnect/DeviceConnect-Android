/*
 AWSIotDeviceApplication.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.core;

import android.app.Application;

import com.amazonaws.regions.Regions;

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

        // ログインフラグがtrueの場合には自動接続を行う
        AWSIotPrefUtil pref = new AWSIotPrefUtil(this);
        if (pref.isAWSLoginFlag()) {
            String accessKey = pref.getAccessKey();
            String secretKey = pref.getSecretKey();
            Regions region = pref.getRegions();
            mIot.connect(accessKey, secretKey, region, new AWSIotController.ConnectCallback() {
                @Override
                public void onConnected(final Exception err) {
                    if (err == null) {
                        mRDCMListManager.updateManagerList(null);
                    }
                }
            });
        }
    }

    @Override
    public void onTerminate() {
        if (mRDCMListManager != null) {
            mRDCMListManager.stopUpdateManagerListTimer();
        }
        super.onTerminate();
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
