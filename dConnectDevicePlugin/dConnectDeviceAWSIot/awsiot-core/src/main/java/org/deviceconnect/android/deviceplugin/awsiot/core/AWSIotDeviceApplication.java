/*
 AWSIotDeviceApplication.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.core;

import android.app.Application;

/**
 * AWS IoT Device Plugin Application.
 *
 * @author NTT DOCOMO, INC.
 */
public class AWSIotDeviceApplication extends Application {

    /** AWSIotコントローラー */
    private final AWSIotController mIot = new AWSIotController();

    /** Instance of {@link RDCMListManager}. */
    private RDCMListManager mRDCMListManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mRDCMListManager = new RDCMListManager(getApplicationContext(), mIot);
        mRDCMListManager.startUpdateManagerListTimer();
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
}
