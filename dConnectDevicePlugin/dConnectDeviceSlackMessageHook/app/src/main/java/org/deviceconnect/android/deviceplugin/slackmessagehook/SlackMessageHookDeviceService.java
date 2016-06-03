/*
 SlackMessageHookDeviceService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.slackmessagehook;

import org.deviceconnect.android.deviceplugin.slackmessagehook.profile.SlackMessageHookProfile;
import org.deviceconnect.android.deviceplugin.slackmessagehook.profile.SlackMessageHookServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.slackmessagehook.profile.SlackMessageHookSystemProfile;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * 本デバイスプラグインのプロファイルをDeviceConnectに登録するサービス.
 * @author NTT DOCOMO, INC.
 */
public class SlackMessageHookDeviceService extends DConnectMessageService {

    @Override
    public void onCreate() {
        super.onCreate();
        addProfile(new SlackMessageHookProfile());
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new SlackMessageHookSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new ServiceInformationProfile(this){};
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new SlackMessageHookServiceDiscoveryProfile(this);
    }

}