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
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.provider.FileManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
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
    private FileManager mFileManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mFileManager = new FileManager(this);

        // テスト用データ作成
        createTestData();

        getServiceProvider().addService(new UnitTestService(SERVICE_ID,
            DEVICE_NAME, getPluginSpec()));
        getServiceProvider().addService(new UnitTestService(SERVICE_ID_SPECIAL_CHARACTERS,
            DEVICE_NAME_SPECIAL_CHARACTERS, getPluginSpec()));
    }

    @Override
    public void onDestroy() {
        File file = new File(mFileManager.getBasePath(), "test.dat");
        file.delete();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        mLogger.info("onStartCommand: intent=" + intent);
        if (intent != null && intent.getExtras() != null) {
            mLogger.info("onStartCommand: extras=" + toString(intent.getExtras()));
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new TestSystemProfile();
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

    private void createTestData() {
        byte[] buf = new byte[1024];
        Arrays.fill(buf, (byte) 0x01);

        FileOutputStream fos = null;
        try {

            File writeFile = new File(mFileManager.getBasePath(), "test.dat");
            fos = new FileOutputStream(writeFile);
            for (int i = 0;i < 1024; i++) {
                fos.write(buf);
            }
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
