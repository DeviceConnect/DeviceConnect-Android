/*
 LinkingProximityProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingEvent;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingEventListener;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingEventManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingRangeEvent;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ProximityProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LinkingProximityProfile extends ProximityProfile {

    private static final int TIMEOUT = 30;//second
    private ScheduledExecutorService mScheduleService = Executors.newScheduledThreadPool(4);

    @Override
    protected boolean onGetOnDeviceProximity(Intent request, final Intent response, String serviceId) {
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
                Bundle proximity = new Bundle();
                proximity.putBundle(PARAM_PROXIMITY, createProximity(range));
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

    @Override
    protected boolean onPutOnDeviceProximity(Intent request, Intent response, String serviceId, String sessionKey) {
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

    @Override
    protected boolean onDeleteOnDeviceProximity(Intent request, Intent response, String serviceId, String sessionKey) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }
        LinkingEventManager.getInstance().remove(request);
        setResult(response, DConnectMessage.RESULT_OK);
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

    private Bundle createProximity(LinkingManager.Range range) {
        Bundle proximity = new Bundle();
        String rangeStr;
        switch (range) {
            case IMMEDIATE:
                rangeStr = "IMMEDIATE";
                break;
            case NEAR:
                rangeStr = "NEAR";
                break;
            case FAR:
                rangeStr = "FAR";
                break;
            case UNKNOWN:
            default:
                rangeStr = "UNKNOWN";
                break;
        }
        //TODO:undefined values on profile.
        proximity.putString("range", rangeStr);
        return proximity;
    }

}
