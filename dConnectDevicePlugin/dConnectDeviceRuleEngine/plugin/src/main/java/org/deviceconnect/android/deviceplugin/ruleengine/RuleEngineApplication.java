/*
 RuleEngineApplication.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.ruleengine;

import android.app.Application;

import org.deviceconnect.android.deviceplugin.ruleengine.utils.DConnectHelper;

/**
 * RuleEngineApplicationクラス.
 * @author NTT DOCOMO, INC.
 */
public class RuleEngineApplication extends Application {
    private static RuleEngineApplication mInstance = null;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        DConnectHelper.INSTANCE.setContext(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        DConnectHelper.INSTANCE.setContext(null);
    }

    public static RuleEngineApplication getInstance() {
        return mInstance;
    }

}
