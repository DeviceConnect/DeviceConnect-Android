/*
 LinkingProximityProfile.java
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
import org.deviceconnect.android.deviceplugin.linking.beacon.data.GattData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingEvent;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingEventListener;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingEventManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingRangeEvent;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ProximityProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.ProximityProfileConstants;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LinkingProximityProfile extends ProximityProfile {

    private static final int TIMEOUT = 30;//second
    private ScheduledExecutorService mScheduleService = Executors.newScheduledThreadPool(4);

    public LinkingProximityProfile(final DConnectMessageService service) {
        LinkingApplication app = (LinkingApplication) service.getApplication();
        LinkingBeaconManager mgr = app.getLinkingBeaconManager();
        mgr.addOnBeaconProximityEventListener(new LinkingBeaconManager.OnBeaconProximityEventListener() {
            @Override
            public void onProximity(LinkingBeacon beacon, GattData gatt) {
                notifyProximityEvent(beacon, gatt);
            }
        });
    }

    @Override
    protected boolean onGetOnDeviceProximity(Intent request, final Intent response, String serviceId) {
        switch (Util.getServiceType(serviceId)) {
            case Util.LINKING_DEVICE:
                return onGetOnDeviceProximityLinkingDevice(request, response, serviceId);
            case Util.LINKING_BEACON:
                return onGetOnDeviceProximityLinkingBeacon(request, response, serviceId);
            case Util.LINKING_APP:
                MessageUtils.setNotSupportProfileError(response);
                break;
            default:
                MessageUtils.setNotFoundServiceError(response);
                break;
        }
        return true;
    }

    @Override
    protected boolean onPutOnDeviceProximity(Intent request, Intent response, String serviceId, String sessionKey) {
        switch (Util.getServiceType(serviceId)) {
            case Util.LINKING_DEVICE:
                return onPutOnDeviceProximityLinkingDevice(request, response, serviceId, sessionKey);
            case Util.LINKING_BEACON:
                return onPutOnDeviceProximityLinkingBeacon(request, response, serviceId, sessionKey);
            case Util.LINKING_APP:
                MessageUtils.setNotSupportProfileError(response);
                break;
            default:
                MessageUtils.setNotFoundServiceError(response);
                break;
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnDeviceProximity(Intent request, Intent response, String serviceId, String sessionKey) {
        switch (Util.getServiceType(serviceId)) {
            case Util.LINKING_DEVICE:
                return onDeleteOnDeviceProximityLinkingDevice(request, response, serviceId, sessionKey);
            case Util.LINKING_BEACON:
                return onDeleteOnDeviceProximityLinkingBeacon(request, response, serviceId, sessionKey);
            case Util.LINKING_APP:
                MessageUtils.setNotSupportProfileError(response);
                break;
            default:
                MessageUtils.setNotFoundServiceError(response);
                break;
        }
        return true;
    }

    private boolean onGetOnDeviceProximityLinkingDevice(final Intent request, final Intent response, final String serviceId) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }
        final LinkingEvent linkingEvent = new LinkingRangeEvent(getContext().getApplicationContext(), device);
        linkingEvent.setEventInfo(request);
        linkingEvent.setLinkingEventListener(new LinkingEventListener() {
            @Override
            public void onReceiveEvent(Event event, Bundle parameters) {
                linkingEvent.invalidate();
                int order = parameters.getInt(LinkingRangeEvent.EXTRA_RANGE);
                LinkingManager.Range range = LinkingManager.Range.values()[order];
                setProximity(response, createProximity(range));
                setResult(response, DConnectMessage.RESULT_OK);
                sendResponse(response);
            }
        });
        linkingEvent.listen();
        mScheduleService.schedule(new Runnable() {
            @Override
            public void run() {
                linkingEvent.invalidate();
                MessageUtils.setTimeoutError(response);
                sendResponse(response);
            }
        }, TIMEOUT, TimeUnit.SECONDS);
        return false;
    }

    private boolean onGetOnDeviceProximityLinkingBeacon(final Intent request, final Intent response, final String serviceId) {
        LinkingBeaconManager mgr = getLinkingBeaconManager();
        LinkingBeacon beacon = LinkingBeaconUtil.findLinkingBeacon(mgr, serviceId);
        if (beacon == null) {
            MessageUtils.setNotSupportProfileError(response);
            return true;
        }

        GattData gattData = beacon.getGattData();
        if (gattData == null) {
            MessageUtils.setNotSupportProfileError(response);
            return true;
        }

        setResult(response, DConnectMessage.RESULT_OK);
        setProximity(response, createProximity(gattData));
        sendResponse(response);

        return true;
    }

    private boolean onPutOnDeviceProximityLinkingDevice(Intent request, Intent response, String serviceId, String sessionKey) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }
        final LinkingEvent linkingEvent = new LinkingRangeEvent(getContext().getApplicationContext(), device);
        linkingEvent.setEventInfo(request);
        linkingEvent.setLinkingEventListener(new LinkingEventListener() {
            @Override
            public void onReceiveEvent(Event event, Bundle parameters) {
                int order = parameters.getInt(LinkingRangeEvent.EXTRA_RANGE);
                LinkingManager.Range range = LinkingManager.Range.values()[order];
                Bundle proximity = new Bundle();
                proximity.putBundle(PARAM_PROXIMITY, createProximity(range));
                sendEvent(event, proximity);
            }
        });
        LinkingEventManager manager = LinkingEventManager.getInstance();
        manager.add(linkingEvent);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    private boolean onPutOnDeviceProximityLinkingBeacon(Intent request, Intent response, String serviceId, String sessionKey) {
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

    private boolean onDeleteOnDeviceProximityLinkingDevice(Intent request, Intent response, String serviceId, String sessionKey) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }
        LinkingEventManager.getInstance().remove(request);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    private boolean onDeleteOnDeviceProximityLinkingBeacon(Intent request, Intent response, String serviceId, String sessionKey) {
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

    private void notifyProximityEvent(LinkingBeacon beacon, GattData gatt) {
        String serviceId = LinkingBeaconUtil.createServiceIdFromLinkingBeacon(beacon);
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                PROFILE_NAME, null, ATTRIBUTE_ON_DEVICE_PROXIMITY);
        if (events != null && events.size() > 0) {
            synchronized (events) {
                for (Event event : events) {
                    Intent intent = EventManager.createEventMessage(event);
                    setProximity(intent, createProximity(gatt));
                    sendEvent(intent, event.getAccessToken());
                }
            }
        }
    }

    private Bundle createProximity(LinkingManager.Range range) {
        Bundle proximity = new Bundle();
        setRange(proximity, getProximityRange(range));
        return proximity;
    }

    private Bundle createProximity(GattData gatt) {
        Bundle proximity = createProximity(gatt.getRange());
        setValue(proximity, calcDistance(gatt) * 100);
        return proximity;
    }

    private ProximityProfileConstants.Range getProximityRange(LinkingManager.Range range) {
        switch (range) {
            case IMMEDIATE:
                return ProximityProfileConstants.Range.IMMEDIATE;
            case NEAR:
                return ProximityProfileConstants.Range.NEAR;
            case FAR:
                return ProximityProfileConstants.Range.FAR;
            case UNKNOWN:
            default:
                return ProximityProfileConstants.Range.UNKNOWN;
        }
    }

    private double calcDistance(GattData gatt) {
      return Math.pow(10.0, (gatt.getTxPower() - gatt.getRssi()) / 20.0);
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
