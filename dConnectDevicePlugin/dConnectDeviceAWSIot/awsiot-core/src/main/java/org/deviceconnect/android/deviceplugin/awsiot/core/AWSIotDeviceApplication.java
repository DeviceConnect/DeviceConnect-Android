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

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public AWSIotController getAWSIotController() {
        return mIot;
    }
}
