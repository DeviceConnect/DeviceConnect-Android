/*
 ChromeCastServiceDiscoveryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.chromecast.ChromeCastService;
import org.deviceconnect.android.deviceplugin.chromecast.R;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastDiscovery;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.ArrayList;
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
    public ChromeCastServiceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        NetworkType deviceType = NetworkType.WIFI;
        String deviceName = getContext().getResources().getString(R.string.device_name);

        ChromeCastDiscovery discovery = ((ChromeCastService) getContext()).getChromeCastDiscovery();
        discovery.registerEvent();
        List<Bundle> services = new ArrayList<Bundle>();
        for (int i = 0; i < discovery.getDeviceNames().size(); i++) {
            Bundle service = new Bundle();
            setId(service, discovery.getDeviceNames().get(i));
            setName(service, deviceName + " (" + discovery.getDeviceNames().get(i) + ")");
            setType(service, deviceType);
            setOnline(service, true);
            setScopes(service, getProfileProvider());
            services.add(service);
        }
        setServices(response, services);
        setResult(response, DConnectMessage.RESULT_OK);
        response.putExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, 
                request.getIntExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, -1));
        response.putExtra(PARAM_SERVICES, services.toArray(new Bundle[services.size()]));
        return true;
    }

    @Override
    protected boolean onPutOnServiceChange(final Intent request, final Intent response, 
                                            final String serviceId, final String sessionKey) {
        EventError error = EventManager.INSTANCE.addEvent(request);
        switch (error) {
        case NONE:
            setResult(response, DConnectMessage.RESULT_OK);
            break;
        case INVALID_PARAMETER:
            MessageUtils.setInvalidRequestParameterError(response);
            break;
        default:
            MessageUtils.setUnknownError(response);
            break;
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnServiceChange(final Intent request, final Intent response,
                                                final String serviceId, final String sessionKey) {
        EventError error = EventManager.INSTANCE.removeEvent(request);
        switch (error) {
        case NONE:
            setResult(response, DConnectMessage.RESULT_OK);
            break;
        case INVALID_PARAMETER:
            MessageUtils.setInvalidRequestParameterError(response);
            break;
        default:
            MessageUtils.setUnknownError(response);
            break;
        }
        return true;
    }
}
