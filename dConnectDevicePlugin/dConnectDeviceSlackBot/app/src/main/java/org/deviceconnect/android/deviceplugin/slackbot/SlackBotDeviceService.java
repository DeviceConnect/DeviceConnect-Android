/*
 SlackBotDeviceService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.slackbot;

import org.deviceconnect.android.deviceplugin.slackbot.profile.SlackBotServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.slackbot.profile.SlackBotSystemProfile;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * 本デバイスプラグインのプロファイルをDeviceConnectに登録するサービス.
 * @author NTT DOCOMO, INC.
 */
public class SlackBotDeviceService extends DConnectMessageService {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new SlackBotSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new ServiceInformationProfile(this){};
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new SlackBotServiceDiscoveryProfile(this);
    }

}