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
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LinkingProximityProfile extends ProximityProfile {

    public LinkingProximityProfile(final DConnectMessageService service) {
        LinkingApplication app = (LinkingApplication) service.getApplication();
        LinkingBeaconManager beaconManager = app.getLinkingBeaconManager();
        beaconManager.addOnBeaconProximityEventListener(new LinkingBeaconManager.OnBeaconProximityEventListener() {
            @Override
            public void onProximity(LinkingBeacon beacon, GattData gatt) {
                notifyProximityEvent(beacon, gatt);
            }
        });

        LinkingDeviceManager deviceManager = app.getLinkingDeviceManager();
        deviceManager.addRangeListener(new LinkingDeviceManager.RangeListener() {
            @Override
            public void onChangeRange(final LinkingDevice device, final LinkingDeviceManager.Range range) {
                notifyProximityEvent(device, range);
            }
        });
    }

    @Override
    protected boolean onGetOnDeviceProximity(final Intent request, final Intent response,final  String serviceId) {
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
    protected boolean onPutOnDeviceProximity(final Intent request, final Intent response,
                                             final String serviceId, final String sessionKey) {
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
    protected boolean onDeleteOnDeviceProximity(final Intent request, final Intent response,
                                                final String serviceId, final String sessionKey) {
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

        final LinkingDeviceManager deviceManager = getLinkingDeviceManager();
        deviceManager.addRangeListener(new RangeListenerImpl(device) {

            private boolean mDestroy;

            private void destroy() {
                if (mDestroy) {
                    return;
                }
                mDestroy = true;

                if (isEmptyEventList()) {
                    deviceManager.stopRange();
                }
                deviceManager.removeRangeListener(this);
            }

            @Override
            public synchronized void run() {
                if (mDestroy) {
                    return;
                }
                MessageUtils.setTimeoutError(response);
                sendResponse(response);
                destroy();
            }

            @Override
            public synchronized void onChangeRange(final LinkingDevice device, final LinkingDeviceManager.Range range) {
                if (mDestroy) {
                    return;
                }

                if (!device.equals(mDevice)) {
                    return;
                }

                setProximity(response, createProximity(range));
                setResult(response, DConnectMessage.RESULT_OK);
                sendResponse(response);
                destroy();
            }
        });
        deviceManager.startRange();
        return false;
    }

    private boolean onGetOnDeviceProximityLinkingBeacon(final Intent request, final Intent response, final String serviceId) {
        LinkingBeacon beacon = getLinkingBeacon(response, serviceId);
        if (beacon == null) {
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

    private boolean onPutOnDeviceProximityLinkingDevice(final Intent request, final Intent response,
                                                        final String serviceId, final String sessionKey) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }

        EventError error = EventManager.INSTANCE.addEvent(request);
        if (error == EventError.NONE) {
            getLinkingDeviceManager().startRange();
            setResult(response, DConnectMessage.RESULT_OK);
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    private boolean onPutOnDeviceProximityLinkingBeacon(final Intent request, final Intent response,
                                                        final String serviceId, final String sessionKey) {
        if (getLinkingBeacon(response, serviceId) == null) {
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

    private boolean onDeleteOnDeviceProximityLinkingDevice(final Intent request, final Intent response,
                                                           final String serviceId, final String sessionKey) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }

        EventError error = EventManager.INSTANCE.removeEvent(request);
        if (error == EventError.NONE) {
            if (isEmptyEventList()) {
                getLinkingDeviceManager().stopRange();
            }
            setResult(response, DConnectMessage.RESULT_OK);
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    private boolean onDeleteOnDeviceProximityLinkingBeacon(final Intent request, final Intent response,
                                                           final String serviceId, final String sessionKey) {
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
                PROFILE_NAME, null, ATTRIBUTE_ON_DEVICE_PROXIMITY);
        return events.isEmpty();
    }

    private LinkingDevice getDevice(final String serviceId, final Intent response) {
        if (serviceId == null || serviceId.length() == 0) {
            MessageUtils.setEmptyServiceIdError(response);
            return null;
        }
        LinkingDeviceManager mgr = getLinkingDeviceManager();
        LinkingDevice device = mgr.findDeviceByBdAddress(serviceId);
        if (device == null) {
            MessageUtils.setIllegalDeviceStateError(response, "device not found");
            return null;
        }
        return device;
    }

    private Bundle createProximity(LinkingDeviceManager.Range range) {
        Bundle proximity = new Bundle();
        setRange(proximity, convertProximityRange(range));
        return proximity;
    }

    private Bundle createProximity(GattData gatt) {
        Bundle proximity = createProximity(gatt.getRange());
        setValue(proximity, calcDistance(gatt) * 100);
        return proximity;
    }

    private void notifyProximityEvent(final LinkingBeacon beacon, final GattData gatt) {
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

    private void notifyProximityEvent(final LinkingDevice device, final LinkingDeviceManager.Range range) {
        String serviceId = device.getBdAddress();
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                PROFILE_NAME, null, ATTRIBUTE_ON_DEVICE_PROXIMITY);
        if (events != null && events.size() > 0) {
            synchronized (events) {
                for (Event event : events) {
                    Intent intent = EventManager.createEventMessage(event);
                    setProximity(intent, createProximity(range));
                    sendEvent(intent, event.getAccessToken());
                }
            }
        }
    }

    private ProximityProfileConstants.Range convertProximityRange(LinkingDeviceManager.Range range) {
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

    private LinkingDeviceManager getLinkingDeviceManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingDeviceManager();
    }

    private LinkingBeaconManager getLinkingBeaconManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingBeaconManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDeviceService service = (LinkingDeviceService) getContext();
        return (LinkingApplication) service.getApplication();
    }

    private abstract class RangeListenerImpl implements Runnable, LinkingDeviceManager.RangeListener {
        protected LinkingDevice mDevice;
        protected ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
        protected ScheduledFuture<?> mScheduledFuture;
        RangeListenerImpl(final LinkingDevice device) {
            mDevice = device;
            mScheduledFuture = mExecutorService.schedule(this, 30, TimeUnit.SECONDS);
        }
    }
}
