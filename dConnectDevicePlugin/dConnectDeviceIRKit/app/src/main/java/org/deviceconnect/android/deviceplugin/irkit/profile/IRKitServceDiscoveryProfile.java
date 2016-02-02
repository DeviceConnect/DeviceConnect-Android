/*
 IRKitServceDiscoveryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.irkit.IRKitDeviceService;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

/**
 * IRKit Network Service Discovery Profile.
 * @author NTT DOCOMO, INC.
 */
public class IRKitServceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * Constructor.
     * @param provider an instance of {@link DConnectProfileProvider}
     */
    public IRKitServceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    public boolean onGetServices(final Intent request, final Intent response) {
        IRKitDeviceService service = (IRKitDeviceService) getContext();
        service.prepareServiceDiscoveryResponse(response);
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
