/*
 TestServiceInformationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.test.profile;

import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceInformationProfile;

/**
 * JUnit用テストデバイスプラグイン、Service Informationプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class TestServiceInformationProfile extends ServiceInformationProfile {

    /**
     * コンストラクタ.
     * @param provider プロファイルプロバイダ
     */
    public TestServiceInformationProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected ConnectState getWifiState(final String serviceId) {
        return ConnectState.OFF;
    }

    @Override
    protected ConnectState getBluetoothState(final String serviceId) {
        return ConnectState.OFF;
    }

    @Override
    protected ConnectState getNFCState(final String serviceId) {
        return ConnectState.OFF;
    }

    @Override
    protected ConnectState getBLEState(final String serviceId) {
        return ConnectState.OFF;
    }
}
