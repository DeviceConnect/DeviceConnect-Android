/*
 * SonyCameraServiceDiscoveryProfile
 * Copyright (c) 2014 NTT DOCOMO,INC.
 * Released under the MIT license
 * http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.sonycamera.profile;

import org.deviceconnect.android.deviceplugin.sonycamera.SonyCameraDeviceService;

import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

/**
 * Sony Camera 用 Network Service Discovery プロファイル.
 * @author NTT DOCOMO, INC.
 */
public class SonyCameraServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * コンストラクタ.
     * @param provider プロファイルプロバイダ
     */
    public SonyCameraServiceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        return ((SonyCameraDeviceService) getContext()).searchSonyCameraDevice(request, response);
    }

    @Override
    protected boolean onPutOnServiceChange(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        EventError error = EventManager.INSTANCE.addEvent(request);
        switch (error) {
        case NONE:
            response.putExtra(IntentDConnectMessage.EXTRA_PROFILE, ServiceDiscoveryProfile.PROFILE_NAME);
            response.putExtra(IntentDConnectMessage.EXTRA_ATTRIBUTE,
                    ServiceDiscoveryProfile.ATTRIBUTE_ON_SERVICE_CHANGE);

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
    protected boolean onDeleteOnServiceChange(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        EventError error = EventManager.INSTANCE.removeEvent(request);
        switch (error) {
        case NONE:
            response.putExtra(IntentDConnectMessage.EXTRA_PROFILE, ServiceDiscoveryProfile.PROFILE_NAME);
            response.putExtra(IntentDConnectMessage.EXTRA_ATTRIBUTE,
                    ServiceDiscoveryProfile.ATTRIBUTE_ON_SERVICE_CHANGE);
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
