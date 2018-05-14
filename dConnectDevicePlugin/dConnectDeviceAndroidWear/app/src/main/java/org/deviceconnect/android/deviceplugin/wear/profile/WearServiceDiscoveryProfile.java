/*
 HeartRateServiceDiscoveryProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.wear.WearManager;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.service.DConnectServiceProvider;

/**
 * Implement ServiceDiscoveryProfile.
 * @author NTT DOCOMO, INC.
 */
public class WearServiceDiscoveryProfile extends ServiceDiscoveryProfile {


    private final DConnectApi mServiceDiscoveryApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            mWearManager.sendWearData();
            appendServiceList(response);
            return true;
        }
    };
    private WearManager mWearManager;
    /**
     * コンストラクタ.
     * @param provider プロファイルプロバイダ
     */
    public WearServiceDiscoveryProfile(final WearManager manager, final DConnectServiceProvider provider) {
        super(provider);
        mWearManager = manager;
        addApi(mServiceDiscoveryApi);
    }

}
