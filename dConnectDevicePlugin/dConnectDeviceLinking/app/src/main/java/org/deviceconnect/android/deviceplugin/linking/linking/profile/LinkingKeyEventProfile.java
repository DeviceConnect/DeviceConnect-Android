/*
 LinkingKeyEventProfile.java
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
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

public class LinkingKeyEventProfile extends KeyEventProfile implements LinkingDestroy {

    private static final String TAG = "LinkingPlugIn";

    public LinkingKeyEventProfile(final DConnectMessageService service) {
        LinkingApplication app = (LinkingApplication) service.getApplication();
        LinkingDeviceManager deviceManager = app.getLinkingDeviceManager();
        deviceManager.addKeyEventListener(mListener);

        addApi(mPutOnDown);
        addApi(mDeleteOnDown);
    }

    private final LinkingDeviceManager.OnKeyEventListener mListener = new LinkingDeviceManager.OnKeyEventListener() {
        @Override
        public void onKeyEvent(final LinkingDevice device, final int keyCode) {
            notifyKeyEvent(device, keyCode);
        }
    };

    private final DConnectApi mPutOnDown = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOWN;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingDevice device = getDevice(response);
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
    };

    private DConnectApi mDeleteOnDown = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOWN;
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
    };

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingKeyEventProfile#destroy: " + getService().getId());
        }
        getLinkingDeviceManager().removeKeyEventListener(mListener);
    }

    private boolean isEmptyEventList(final LinkingDevice device) {
        List<Event> events = EventManager.INSTANCE.getEventList(
                device.getBdAddress(), PROFILE_NAME, null, ATTRIBUTE_ON_DOWN);
        return events.isEmpty();
    }

    private LinkingDevice getDevice() {
        return ((LinkingDeviceService) getService()).getLinkingDevice();
    }

    private LinkingDevice getDevice(final Intent response) {
        LinkingDevice device = getDevice();

        if (!device.isConnected()) {
            MessageUtils.setIllegalDeviceStateError(response, "device not connected");
            return null;
        }

        return device;
    }
    private Bundle createKeyEvent(final int keyCode) {
        Bundle keyEvent = new Bundle();
        keyEvent.putString(PARAM_ID, String.valueOf(KeyEventProfile.KEYTYPE_STD_KEY + keyCode));
        return keyEvent;
    }

    private void setKeyEvent(final Intent intent, final Bundle keyEvent) {
        intent.putExtra(PARAM_KEYEVENT, keyEvent);
    }

    private void notifyKeyEvent(final LinkingDevice device, final int keyCode) {
        if (!device.equals(getDevice())) {
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "notifyKeyEvent: " + device.getDisplayName() + "[" + keyCode + "]");
        }
        String serviceId = device.getBdAddress();
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                PROFILE_NAME, null, ATTRIBUTE_ON_DOWN);
        if (events != null && events.size() > 0) {
            for (Event event : events) {
                Intent intent = EventManager.createEventMessage(event);
                setKeyEvent(intent, createKeyEvent(keyCode));
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
}
