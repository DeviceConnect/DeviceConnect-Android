/*
 LinkingProximityProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDevicePluginService;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.deviceplugin.linking.linking.service.LinkingDeviceService;
import org.deviceconnect.android.deviceplugin.linking.LinkingDestroy;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ProximityProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

public class LinkingProximityProfile extends ProximityProfile implements LinkingDestroy {

    private static final String TAG = "LinkingPlugIn";

    public LinkingProximityProfile(final DConnectMessageService service) {
        LinkingApplication app = (LinkingApplication) service.getApplication();
        LinkingDeviceManager deviceManager = app.getLinkingDeviceManager();
        deviceManager.addRangeListener(mListener);

        addApi(mGetOnDeviceProximity);
        addApi(mPutOnDeviceProximity);
        addApi(mDeleteOnDeviceProximity);
    }

    private final LinkingDeviceManager.OnRangeListener mListener = new LinkingDeviceManager.OnRangeListener() {
        @Override
        public void onChangeRange(final LinkingDevice device, final LinkingDeviceManager.Range range) {
            notifyProximityEvent(device, range);
        }
    };

    private final DConnectApi mGetOnDeviceProximity = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_PROXIMITY;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }

            final LinkingDeviceManager deviceManager = getLinkingDeviceManager();
            deviceManager.addRangeListener(new OnRangeListenerImpl(device) {

                @Override
                public void onCleanup() {
                    if (isEmptyEventList(mDevice)) {
                        deviceManager.stopRange(mDevice);
                    }
                    deviceManager.removeRangeListener(this);
                }

                @Override
                public synchronized void onTimeout() {
                    if (mCleanupFlag) {
                        return;
                    }
                    MessageUtils.setTimeoutError(response);
                    sendResponse(response);
                }

                @Override
                public synchronized void onChangeRange(final LinkingDevice device, final LinkingDeviceManager.Range range) {
                    if (mCleanupFlag || !device.equals(mDevice)) {
                        return;
                    }

                    setResult(response, DConnectMessage.RESULT_OK);
                    setProximity(response, createProximity(range));
                    sendResponse(response);
                    cleanup();
                }
            });
            deviceManager.startRange(device);
            return false;
        }
    };

    private final DConnectApi mPutOnDeviceProximity = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_PROXIMITY;
        }
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }

            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                getLinkingDeviceManager().startRange(device);
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnDeviceProximity = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_PROXIMITY;
        }
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }

            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                if (isEmptyEventList(device)) {
                    getLinkingDeviceManager().stopRange(device);
                }
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingProximityProfile#destroy: " + getService().getId());
        }
        getLinkingDeviceManager().removeRangeListener(mListener);
    }

    private LinkingDevice getDevice(final Intent response) {
        LinkingDevice device = ((LinkingDeviceService) getService()).getLinkingDevice();

        if (!device.isConnected()) {
            MessageUtils.setIllegalDeviceStateError(response, "device not connected");
            return null;
        }

        return device;
    }

    private boolean isEmptyEventList(final LinkingDevice device) {
        List<Event> events = EventManager.INSTANCE.getEventList(
                device.getBdAddress(), PROFILE_NAME, null, ATTRIBUTE_ON_DEVICE_PROXIMITY);
        return events.isEmpty();
    }

    private Bundle createProximity(final LinkingDeviceManager.Range range) {
        Bundle proximity = new Bundle();
        setRange(proximity, convertProximityRange(range));
        return proximity;
    }

    private Range convertProximityRange(final LinkingDeviceManager.Range range) {
        switch (range) {
            case IMMEDIATE:
                return Range.IMMEDIATE;
            case NEAR:
                return Range.NEAR;
            case FAR:
                return Range.FAR;
            case UNKNOWN:
            default:
                return Range.UNKNOWN;
        }
    }

    private void notifyProximityEvent(final LinkingDevice device, final LinkingDeviceManager.Range range) {
        String serviceId = device.getBdAddress();
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                PROFILE_NAME, null, ATTRIBUTE_ON_DEVICE_PROXIMITY);
        if (events != null && events.size() > 0) {
            for (Event event : events) {
                Intent intent = EventManager.createEventMessage(event);
                setProximity(intent, createProximity(range));
                sendEvent(intent, event.getAccessToken());
            }
        }
    }

    private LinkingDeviceManager getLinkingDeviceManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingDeviceManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDevicePluginService service = (LinkingDevicePluginService) getContext();
        return (LinkingApplication) service.getApplication();
    }

    private abstract class OnRangeListenerImpl extends TimeoutSchedule implements LinkingDeviceManager.OnRangeListener {
        OnRangeListenerImpl(final LinkingDevice device) {
            super(device);
        }
    }
}
