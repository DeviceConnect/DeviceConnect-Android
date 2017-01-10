/*
 DConnectApplication.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.uiapp;

import android.app.Application;

import org.deviceconnect.android.uiapp.utils.Settings;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.message.DConnectSDKFactory;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.BatteryProfileConstants;
import org.deviceconnect.profile.CanvasProfileConstants;
import org.deviceconnect.profile.ConnectProfileConstants;
import org.deviceconnect.profile.DeviceOrientationProfileConstants;
import org.deviceconnect.profile.FileDescriptorProfileConstants;
import org.deviceconnect.profile.FileProfileConstants;
import org.deviceconnect.profile.HumanDetectProfileConstants;
import org.deviceconnect.profile.KeyEventProfileConstants;
import org.deviceconnect.profile.LightProfileConstants;
import org.deviceconnect.profile.MediaPlayerProfileConstants;
import org.deviceconnect.profile.MediaStreamRecordingProfileConstants;
import org.deviceconnect.profile.NotificationProfileConstants;
import org.deviceconnect.profile.PhoneProfileConstants;
import org.deviceconnect.profile.ProximityProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.ServiceInformationProfileConstants;
import org.deviceconnect.profile.SettingsProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;
import org.deviceconnect.profile.TouchProfileConstants;
import org.deviceconnect.profile.VibrationProfileConstants;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DConnectApplication extends Application {

    /**
     * Device Connect Managerとアクセスするインターフェース.
     */
    private DConnectSDK mDConnectSK;

    /**
     * Local OAuthに使用するスコープ一覧.
     */
    public static final List<String> SCOPES = new ArrayList<String>() {
        {
            add(AuthorizationProfileConstants.PROFILE_NAME);
            add(BatteryProfileConstants.PROFILE_NAME);
            add(CanvasProfileConstants.PROFILE_NAME);
            add(ConnectProfileConstants.PROFILE_NAME);
            add(DeviceOrientationProfileConstants.PROFILE_NAME);
            add(FileDescriptorProfileConstants.PROFILE_NAME);
            add(FileProfileConstants.PROFILE_NAME);
            add(HumanDetectProfileConstants.PROFILE_NAME);
            add(KeyEventProfileConstants.PROFILE_NAME);
            add(LightProfileConstants.PROFILE_NAME);
            add(MediaPlayerProfileConstants.PROFILE_NAME);
            add(MediaStreamRecordingProfileConstants.PROFILE_NAME);
            add(ServiceDiscoveryProfileConstants.PROFILE_NAME);
            add(ServiceInformationProfileConstants.PROFILE_NAME);
            add(NotificationProfileConstants.PROFILE_NAME);
            add(PhoneProfileConstants.PROFILE_NAME);
            add(ProximityProfileConstants.PROFILE_NAME);
            add(SettingsProfileConstants.PROFILE_NAME);
            add(SystemProfileConstants.PROFILE_NAME);
            add(TouchProfileConstants.PROFILE_NAME);
            add(VibrationProfileConstants.PROFILE_NAME);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Settings.getInstance().load(this);
    }

    public DConnectSDK getDConnectSK() {
        if (mDConnectSK == null) {
            initDConnectSDK(Settings.getInstance().getSDKType());
        }
        mDConnectSK.setSSL(Settings.getInstance().isSSL());
        return mDConnectSK;
    }

    public void initDConnectSDK(final String type) {
        if (type.equals(getString(R.string.activity_settings_sdk_entry1))) {
            mDConnectSK = DConnectSDKFactory.create(this, DConnectSDKFactory.Type.HTTP);
        } else {
            mDConnectSK = DConnectSDKFactory.create(this, DConnectSDKFactory.Type.INTENT);
        }
        mDConnectSK.setHost(Settings.getInstance().getHostName());
        mDConnectSK.setPort(Settings.getInstance().getPort());
        String accessToken = Settings.getInstance().getAccessToken();
        if (accessToken != null) {
            mDConnectSK.setAccessToken(accessToken);
        }
    }
}
