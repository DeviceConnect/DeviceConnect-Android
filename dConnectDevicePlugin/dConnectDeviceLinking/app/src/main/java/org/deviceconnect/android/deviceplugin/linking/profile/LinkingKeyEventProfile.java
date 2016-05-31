/*
 LinkingLightProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingEvent;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingEventListener;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingEventManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingKeyEvent;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.message.DConnectMessage;

public class LinkingKeyEventProfile extends KeyEventProfile {

    @Override
    protected boolean onGetOnDown(Intent request, Intent response, String serviceId) {
        return super.onGetOnDown(request, response, serviceId);
    }

    @Override
    protected boolean onGetOnUp(Intent request, Intent response, String serviceId) {
        return super.onGetOnUp(request, response, serviceId);
    }

    @Override
    protected boolean onPutOnDown(Intent request, Intent response, String serviceId, final String sessionKey) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }
        final LinkingEvent linkingEvent = new LinkingKeyEvent(getContext().getApplicationContext(), device);
        linkingEvent.setEventInfo(request);
        linkingEvent.setLinkingEventListener(new LinkingEventListener() {
            @Override
            public void onReceiveEvent(Event event, Bundle parameters) {
                Log.e("ABC", "AAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                int keyCode = parameters.getInt(LinkingKeyEvent.EXTRA_KEY_CODE);
                Bundle keyEvent = new Bundle();
                keyEvent.putBundle(PARAM_KEYEVENT, createKeyEvent(keyCode));
                sendEvent(event, keyEvent);
            }
        });
        LinkingEventManager manager = LinkingEventManager.getInstance();
        manager.add(linkingEvent);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onPutOnUp(Intent request, Intent response, String serviceId, String sessionKey) {
        return super.onPutOnUp(request, response, serviceId, sessionKey);
    }

    @Override
    protected boolean onDeleteOnDown(Intent request, Intent response, String serviceId, String sessionKey) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }
        LinkingEventManager.getInstance().remove(request);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onDeleteOnUp(Intent request, Intent response, String serviceId, String sessionKey) {
        return super.onDeleteOnUp(request, response, serviceId, sessionKey);
    }


    private LinkingDevice getDevice(String serviceId, Intent response) {
        if (serviceId == null || serviceId.length() == 0) {
            MessageUtils.setEmptyServiceIdError(response);
            return null;
        }
        LinkingDevice device = LinkingUtil.getLinkingDevice(getContext(), serviceId);
        if (device == null) {
            MessageUtils.setIllegalDeviceStateError(response, "device not found");
            return null;
        }
        return device;
    }

    private Bundle createKeyEvent(int keyCode) {
        Bundle keyEvent = new Bundle();
        keyEvent.putString(PARAM_ID, String.valueOf(keyCode));
        keyEvent.putString(PARAM_CONFIG, "");
        return keyEvent;
    }
}
