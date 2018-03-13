/*
 ChromeCastServiceDiscoveryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.profile;

import android.content.Intent;

import com.google.android.gms.cast.CastDevice;

import org.deviceconnect.android.deviceplugin.chromecast.ChromeCastDeviceService;
import org.deviceconnect.android.deviceplugin.chromecast.ChromeCastService;
import org.deviceconnect.android.deviceplugin.chromecast.R;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastDiscovery;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceProvider;

import java.util.Iterator;
import java.util.List;

/**
 * Network Service Discovery プロファイル (Chromecast).
 * <p>
 * Chromecastの検索機能を提供する
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class ChromeCastServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * コンストラクタ.
     * 
     * @param provider プロファイルプロバイダ
     */
    public ChromeCastServiceDiscoveryProfile(final DConnectServiceProvider provider) {
        super(provider);
        addApi(mServiceDiscoveryApi);
    }

    private final DConnectApi mServiceDiscoveryApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            ChromeCastDiscovery discovery = ((ChromeCastService) getContext()).getChromeCastDiscovery();
            List<DConnectService> disappeared = getServiceProvider().getServiceList();
            for (int i = 0; i < discovery.getDeviceNames().size(); i++) {

                CastDevice cast = discovery.getDeviceNames().get(i);
                DConnectService castService = getServiceProvider().getService(cast.getDeviceId());
                if (castService == null) {
                    castService = new ChromeCastDeviceService(cast);
                    getServiceProvider().addService(castService);
                } else {
                    for (int j = 0; j < disappeared.size(); j++) {
                        DConnectService cache = disappeared.get(j);
                        if (cache.getId().equals(castService.getId())) {
                            disappeared.remove(j);
                            break;
                        }
                    }
                }
            }

            // レスポンス作成.
            appendServiceList(response);

            return true;
        }
    };

    private String getDeviceName(final String name) {
        return getContext().getResources().getString(R.string.device_name, name);
    }
}
