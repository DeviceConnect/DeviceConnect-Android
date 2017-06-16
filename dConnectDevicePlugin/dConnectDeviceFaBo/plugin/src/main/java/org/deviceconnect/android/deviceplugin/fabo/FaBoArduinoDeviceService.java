/*
 FaBoDeviceService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fabo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.fabo.device.FaBoDeviceControl;
import org.deviceconnect.android.deviceplugin.fabo.device.arduino.FaBoUsbDeviceControl;
import org.deviceconnect.android.deviceplugin.fabo.setting.FaBoSettingActivity;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * 本デバイスプラグインのプロファイルをDeviceConnectに登録するサービス.
 *
 * @author NTT DOCOMO, INC.
 */
public class FaBoArduinoDeviceService extends FaBoDeviceService {

    /**
     * Local OAuthの設定を切り替えるアクション.
     */
    public static final String ACTION_SET_LOCAL_OAUTH = "org.deviceconnect.android.deviceplugin.fabo.ACTION_SET_LOCAL_OAUTH";

    /**
     * FaBoの設定を保持するクラス.
     */
    private FaBoSettings mFaBoSettings;

    @Override
    public void onCreate() {
        super.onCreate();
        mFaBoSettings = new FaBoSettings(this);
        setUseLocalOAuth(mFaBoSettings.isUseLocalOAuth());
    }

    @Override
    public void onDestroy() {
        mFaBoSettings = null;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent != null && ACTION_SET_LOCAL_OAUTH.equals(intent.getAction())) {
            setUseLocalOAuth(mFaBoSettings.isUseLocalOAuth());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected FaBoDeviceControl createFaBoDeviceControl() {
        return new FaBoUsbDeviceControl(this);
    }


    @Override
    protected SystemProfile getSystemProfile() {
        return new FaBoSystemProfile();
    }

    /**
     * Arduino用のSystemプロファイル.
     */
    private class FaBoSystemProfile extends SystemProfile {
        @Override
        protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
            return FaBoSettingActivity.class;
        }
    }
}
