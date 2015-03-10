/*
 HostServiceDiscoveryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import java.util.ArrayList;
import java.util.List;

import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import android.content.Intent;
import android.os.Bundle;

/**
 * ホストデバイスプラグイン, Network Service Discovery プロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * デバイスプラグインID.
     */
    public static final String SERVICE_ID = "Host";

    /**
     * デバイス名.
     */
    public static final String DEVICE_NAME = "Host";

    /**
     * テスト用デバイスタイプ.
     */
    public static final String DEVICE_TYPE = "Wifi";

    /**
     * オンライン状態.
     */
    public static final boolean DEVICE_ONLINE = true;

    /**
     * コンフィグ.
     */
    public static final String DEVICE_CONFIG = "HostConfig";

    /**
     * コンストラクタ.
     * 
     * @param provider プロファイルプロバイダ
     */
    public HostServiceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    public boolean onGetServices(final Intent request, final Intent response) {

        List<Bundle> services = new ArrayList<Bundle>();

        Bundle service = new Bundle();
        setId(service, SERVICE_ID);
        setName(service, DEVICE_NAME);
        setType(service, DEVICE_TYPE);
        setOnline(service, DEVICE_ONLINE);
        setConfig(service, DEVICE_CONFIG);
        setScopes(service, getProfileProvider());
        services.add(service);

        setResult(response, DConnectMessage.RESULT_OK);
        response.putExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE,
                request.getIntExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, -1));
        response.putExtra(PARAM_SERVICES, services.toArray(new Bundle[services.size()]));

        return true;
    }
}
