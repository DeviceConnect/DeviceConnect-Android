/*
 LinkingLightProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDeviceService;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconUtil;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingEvent;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingEventListener;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingEventManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingKeyEvent;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

public class LinkingKeyEventProfile extends KeyEventProfile {

    public LinkingKeyEventProfile(DConnectMessageService service) {
        LinkingApplication app = (LinkingApplication) service.getApplication();
        LinkingBeaconManager mgr = app.getLinkingBeaconManager();
        mgr.addOnBeaconButtonEventListener(new LinkingBeaconManager.OnBeaconButtonEventListener() {
            @Override
            public void onClickButton(LinkingBeacon beacon, int keyCode, long timeStamp) {
                notifyKeyEvent(beacon, keyCode, timeStamp);
            }
        });
    }

    @Override
    protected boolean onPutOnDown(Intent request, Intent response, String serviceId, final String sessionKey) {
        switch (Util.getServiceType(serviceId)) {
            case Util.LINKING_DEVICE:
                return onPutOnDownLinkingDevice(request, response, serviceId, sessionKey);
            case Util.LINKING_BEACON:
                return onPutOnDownLinkingBeacon(request, response, serviceId, sessionKey);
            case Util.LINKING_APP:
            default:
                MessageUtils.setNotFoundServiceError(response);
                break;
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnDown(Intent request, Intent response, String serviceId, String sessionKey) {
        switch (Util.getServiceType(serviceId)) {
            case Util.LINKING_DEVICE:
                return onDeleteOnDownLinkingDevice(request, response, serviceId, sessionKey);
            case Util.LINKING_BEACON:
                return onDeleteOnDownLinkingBeacon(request, response, serviceId, sessionKey);
            case Util.LINKING_APP:
            default:
                MessageUtils.setNotFoundServiceError(response);
                break;
        }
        return true;
    }

    private boolean onPutOnDownLinkingDevice(Intent request, Intent response, String serviceId, final String sessionKey) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }
        final LinkingEvent linkingEvent = new LinkingKeyEvent(getContext().getApplicationContext(), device);
        linkingEvent.setEventInfo(request);
        linkingEvent.setLinkingEventListener(new LinkingEventListener() {
            @Override
            public void onReceiveEvent(Event event, Bundle parameters) {
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

    private boolean onPutOnDownLinkingBeacon(Intent request, Intent response, String serviceId, final String sessionKey) {
        LinkingBeacon beacon = getLinkingBeacon(response, serviceId);
        if (beacon == null) {
            return true;
        }

        EventError error = EventManager.INSTANCE.addEvent(request);
        if (error == EventError.NONE) {
            setResult(response, DConnectMessage.RESULT_OK);
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    private boolean onDeleteOnDownLinkingDevice(Intent request, Intent response, String serviceId, String sessionKey) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }
        LinkingEventManager.getInstance().remove(request);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    private boolean onDeleteOnDownLinkingBeacon(Intent request, Intent response, String serviceId, String sessionKey) {
        EventError error = EventManager.INSTANCE.removeEvent(request);
        if (error == EventError.NONE) {
            setResult(response, DConnectMessage.RESULT_OK);
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
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
        keyEvent.putString(PARAM_ID, String.valueOf(KeyEventProfile.KEYTYPE_STD_KEY + keyCode));
        return keyEvent;
    }

    private Bundle createKeyEvent(int keyCode, long timeStamp) {
        Bundle keyEvent = createKeyEvent(keyCode);
        keyEvent.putString(PARAM_CONFIG, "" + timeStamp);
        return keyEvent;
    }

    private void notifyKeyEvent(LinkingBeacon beacon, int keyCode, long timeStamp) {
        String serviceId = LinkingBeaconUtil.createServiceIdFromLinkingBeacon(beacon);
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                PROFILE_NAME, null, ATTRIBUTE_ON_DOWN);
        if (events != null && events.size() > 0) {
            synchronized (events) {
                for (Event event : events) {
                    Intent intent = EventManager.createEventMessage(event);
                    setKeyEvent(intent, createKeyEvent(keyCode, timeStamp));
                    sendEvent(intent, event.getAccessToken());
                }
            }
        }
    }

    private void setKeyEvent(Intent intent, Bundle keyEvent) {
        intent.putExtra(PARAM_KEYEVENT, keyEvent);
    }

    private LinkingBeacon getLinkingBeacon(Intent response, String serviceId) {
        LinkingBeaconManager mgr = getLinkingBeaconManager();
        LinkingBeacon beacon = LinkingBeaconUtil.findLinkingBeacon(mgr, serviceId);
        if (beacon == null) {
            MessageUtils.setNotSupportProfileError(response);
            return null;
        }

        if (!beacon.isOnline()) {
            MessageUtils.setIllegalDeviceStateError(response, beacon.getDisplayName() + " is offline.");
            return null;
        }
        return beacon;
    }

    private LinkingBeaconManager getLinkingBeaconManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingBeaconManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDeviceService service = (LinkingDeviceService) getContext();
        return (LinkingApplication) service.getApplication();
    }
}
