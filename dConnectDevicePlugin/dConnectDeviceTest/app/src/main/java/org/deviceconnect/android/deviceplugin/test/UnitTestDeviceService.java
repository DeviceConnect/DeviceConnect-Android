/*
 DeviceTestService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.test;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.test.profile.TestSystemProfile;
import org.deviceconnect.android.deviceplugin.test.service.UnitTestService;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;

import java.util.Iterator;
import java.util.logging.Logger;


/**
 * テスト用プロファイルを公開するためのサービス.
 * @author NTT DOCOMO, INC.
 */
public class UnitTestDeviceService extends DConnectMessageService {

    private static final String SERVICE_ID = "test_service_id";
    private static final String DEVICE_NAME = "Test Success Device";
    private static final String SERVICE_ID_SPECIAL_CHARACTERS = "!#$'()-~¥@[;+:*],._/=?&%^|`\"{}<>";
    private static final String DEVICE_NAME_SPECIAL_CHARACTERS = "Test Service ID Special Characters";

    /** ロガー. */
    private Logger mLogger = Logger.getLogger("dconnect.dplugin.test");

    @Override
    public void onCreate() {
        super.onCreate();
        EventManager.INSTANCE.setController(new MemoryCacheController());
        LocalOAuth2Main.initialize(getApplicationContext());

        getServiceProvider().addService(new UnitTestService(SERVICE_ID,
            DEVICE_NAME, getPluginSpec()));
        getServiceProvider().addService(new UnitTestService(SERVICE_ID_SPECIAL_CHARACTERS,
            DEVICE_NAME_SPECIAL_CHARACTERS, getPluginSpec()));
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        mLogger.info("onStartCommand: intent=" + intent);
        if (intent != null) {
            mLogger.info("onStartCommand: extras=" + toString(intent.getExtras()));
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * JSON文字列に変換する.
     * @param bundle Bundle
     * @return JSON String
     */
    private String toString(final Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Iterator<String> it = bundle.keySet().iterator(); it.hasNext();) {
            String key = it.next();
            sb.append(key + ":" + bundle.get(key));
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new TestSystemProfile();
    }

}
