/*
 LinkingKeyEventProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDevicePluginService;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconUtil;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LinkingKeyEventProfile extends KeyEventProfile {

    public LinkingKeyEventProfile(DConnectMessageService service) {
        LinkingApplication app = (LinkingApplication) service.getApplication();
        LinkingBeaconManager beaconManager = app.getLinkingBeaconManager();
        beaconManager.addOnBeaconButtonEventListener(new LinkingBeaconManager.OnBeaconButtonEventListener() {
            @Override
            public void onClickButton(LinkingBeacon beacon, int keyCode, long timeStamp) {
                notifyKeyEvent(beacon, keyCode, timeStamp);
            }
        });

        LinkingDeviceManager deviceManager = app.getLinkingDeviceManager();
        deviceManager.addKeyEventListener(new LinkingDeviceManager.OnKeyEventListener() {
            @Override
            public void onKeyEvent(LinkingDevice device, int keyCode) {
                notifyKeyEvent(device, keyCode);
            }
        });
    }

    @Override
    protected boolean onPutOnDown(final Intent request, final Intent response, final String serviceId, final String sessionKey) {
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
    protected boolean onDeleteOnDown(final Intent request, final Intent response, final String serviceId, final String sessionKey) {
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

    private boolean onPutOnDownLinkingDevice(final Intent request, final Intent response, final String serviceId, final String sessionKey) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }

        EventError error = EventManager.INSTANCE.addEvent(request);
        if (error == EventError.NONE) {
            getLinkingDeviceManager().startKeyEvent(device);
            setResult(response, DConnectMessage.RESULT_OK);
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    private boolean onPutOnDownLinkingBeacon(final Intent request, final Intent response, final String serviceId, final String sessionKey) {
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

    private boolean onDeleteOnDownLinkingDevice(final Intent request, final Intent response, final String serviceId, final String sessionKey) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }

        EventError error = EventManager.INSTANCE.removeEvent(request);
        if (error == EventError.NONE) {
            if (isEmptyEventList()) {
                getLinkingDeviceManager().stopKeyEvent(device);
            }
            setResult(response, DConnectMessage.RESULT_OK);
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    private boolean onDeleteOnDownLinkingBeacon(final Intent request, final Intent response, final String serviceId, final String sessionKey) {
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

    private boolean isEmptyEventList() {
        List<Event> events = EventManager.INSTANCE.getEventList(
                PROFILE_NAME, null, ATTRIBUTE_ON_DOWN);
        return events.isEmpty();
    }

    private LinkingDevice getDevice(final String serviceId, final Intent response) {
        if (serviceId == null || serviceId.length() == 0) {
            MessageUtils.setEmptyServiceIdError(response);
            return null;
        }
        LinkingDevice device = getLinkingDeviceManager().findDeviceByBdAddress(serviceId);
        if (device == null) {
            MessageUtils.setIllegalDeviceStateError(response, "device not found");
            return null;
        }
        return device;
    }

    private Bundle createKeyEvent(final int keyCode) {
        Bundle keyEvent = new Bundle();
        keyEvent.putString(PARAM_ID, String.valueOf(KeyEventProfile.KEYTYPE_STD_KEY + keyCode));
        return keyEvent;
    }

    private Bundle createKeyEvent(final int keyCode, final long timeStamp) {
        Bundle keyEvent = createKeyEvent(keyCode);
        keyEvent.putString(PARAM_CONFIG, "" + timeStamp);
        return keyEvent;
    }

    private void setKeyEvent(final Intent intent, final Bundle keyEvent) {
        intent.putExtra(PARAM_KEYEVENT, keyEvent);
    }

    private void notifyKeyEvent(final LinkingBeacon beacon, final int keyCode, final long timeStamp) {
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

    private void notifyKeyEvent(final LinkingDevice device, final int keyCode) {
        String serviceId = device.getBdAddress();
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                PROFILE_NAME, null, ATTRIBUTE_ON_DOWN);
        if (events != null && events.size() > 0) {
            synchronized (events) {
                for (Event event : events) {
                    Intent intent = EventManager.createEventMessage(event);
                    setKeyEvent(intent, createKeyEvent(keyCode));
                    sendEvent(intent, event.getAccessToken());
                }
            }
        }
    }

    private LinkingBeacon getLinkingBeacon(final Intent response, final String serviceId) {
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

    private LinkingDeviceManager getLinkingDeviceManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingDeviceManager();
    }

    private LinkingBeaconManager getLinkingBeaconManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingBeaconManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDevicePluginService service = (LinkingDevicePluginService) getContext();
        return (LinkingApplication) service.getApplication();
    }


    private abstract class OnKeyEventListenerImpl implements Runnable, LinkingDeviceManager.OnKeyEventListener {
        protected LinkingDevice mDevice;
        protected ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
        protected ScheduledFuture<?> mScheduledFuture;
        OnKeyEventListenerImpl(final LinkingDevice device) {
            mDevice = device;
            mScheduledFuture = mExecutorService.schedule(this, 30, TimeUnit.SECONDS);
        }
    }
}
