/*
FaBoServceDiscoveryProfile
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.fabo.profile;

import android.content.Intent;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

/**
 * Service Discovery プロファイルの実装クラス.
 * @author docomo
 */
public class FaBoServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    /** サンプルのサービスID. */
    public static final String SERVICE_ID = "gpio_service_id";

    /** サンプルのデバイス名. */
    private static final String DEVICE_NAME = "FABO Device";

    public FaBoServiceDiscoveryProfile(DConnectProfileProvider provider) {
        super(provider);
    }

    /**
     * Service Discoveryプロファイル.
     * [/servicediscovery]に対応するメソッド.
     * @param request リクエスト
     * @param response レスポンス
     * @return 即座に返却する場合はtrue, 非同期に返却する場合はfalse
     */
    @Override
    public boolean onGetServices(final Intent request, final Intent response) {
        List<Bundle> services = new ArrayList<Bundle>();
        Bundle service = new Bundle();
        setId(service, SERVICE_ID);
        setName(service, DEVICE_NAME);
        setType(service, NetworkType.WIFI);
        setOnline(service, true);
        services.add(service);
        setServices(response, services.toArray(new Bundle[services.size()]));
        setResult(response, IntentDConnectMessage.RESULT_OK);
        return true;
    }
}
