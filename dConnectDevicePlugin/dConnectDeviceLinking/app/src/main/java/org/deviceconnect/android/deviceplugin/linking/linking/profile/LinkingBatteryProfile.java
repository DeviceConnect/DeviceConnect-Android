/*
 LinkingBatteryProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.lib.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDestroy;
import org.deviceconnect.android.deviceplugin.linking.LinkingDevicePluginService;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.deviceplugin.linking.linking.service.LinkingDeviceService;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.BatteryProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

public class LinkingBatteryProfile extends BatteryProfile implements LinkingDestroy {

    private static final String TAG = "LinkingPlugIn";

    public LinkingBatteryProfile() {
        addApi(mGetAll);
        addApi(mGetLevel);
        addApi(mPutOnBatteryChange);
        addApi(mDeleteOnBatteryChange);
    }

    private final LinkingDeviceManager.OnBatteryListener mListener = (device, lowBatteryFlag, batteryLevel) -> {
        notifyBattery(device, batteryLevel);
    };

    private final DConnectApi mGetAll = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return getBattery(request, response);
        }
    };

    private final DConnectApi mGetLevel = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_LEVEL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return getBattery(request, response);
        }
    };

    private final DConnectApi mPutOnBatteryChange = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_BATTERY_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }

            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                getLinkingDeviceManager().enableListenBattery(device, mListener);
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnBatteryChange = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_BATTERY_CHANGE;
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
                    getLinkingDeviceManager().disableListenBattery(device, mListener);
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
            Log.i(TAG, "LinkingBatteryProfile#destroy: " + getService().getId());
        }
        getLinkingDeviceManager().disableListenBattery(getDevice(), mListener);
    }

    private boolean getBattery(final Intent request, final Intent response) {
        LinkingDevice device = getDevice(response);
        if (device == null) {
            return true;
        }

        final LinkingDeviceManager deviceManager = getLinkingDeviceManager();
        deviceManager.enableListenBattery(device, new OnBatteryListenerImpl(device) {
            @Override
            public void onCleanup() {
                deviceManager.disableListenBattery(mDevice, this);
            }

            @Override
            public void onTimeout() {
                if (mCleanupFlag) {
                    return;
                }

                MessageUtils.setTimeoutError(response);
                sendResponse(response);
            }

            @Override
            public void onBattery(LinkingDevice device, boolean lowBatteryFlag, float batteryLevel) {
                if (mCleanupFlag || !mDevice.equals(device)) {
                    return;
                }

                setBatteryToResponse(response, batteryLevel);
                sendResponse(response);
                cleanup();
            }
        });
        return false;
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

        if (!device.isSupportBattery()) {
            MessageUtils.setIllegalDeviceStateError(response, "device has not battery");
            return null;
        }

        return device;
    }

    private boolean isEmptyEventList(final LinkingDevice device) {
        List<Event> events = EventManager.INSTANCE.getEventList(
                device.getBdAddress(), PROFILE_NAME, null, ATTRIBUTE_ON_BATTERY_CHANGE);
        return events.isEmpty();
    }

    private  void notifyBattery(final LinkingDevice device, final float batteryLevel) {
        String serviceId = device.getBdAddress();
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                PROFILE_NAME, null, ATTRIBUTE_ON_BATTERY_CHANGE);
        if (events != null && events.size() > 0) {
            for (Event event : events) {
                Intent intent = EventManager.createEventMessage(event);
                setBattery(intent, createBattery(batteryLevel));
                sendEvent(intent, event.getAccessToken());
            }
        }
    }

    private Bundle createBattery(final float batteryLevel) {
        Bundle battery = new Bundle();
        setLevel(battery, batteryLevel / 100.0f);
        return battery;
    }

    private void setBatteryToResponse(final Intent response, final float batteryLevel) {
        setResult(response, DConnectMessage.RESULT_OK);
        setLevel(response, batteryLevel / 100.0f);
    }

    private LinkingDeviceManager getLinkingDeviceManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingDeviceManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDevicePluginService service = (LinkingDevicePluginService) getContext();
        return (LinkingApplication) service.getApplication();
    }

    private abstract class OnBatteryListenerImpl extends TimeoutSchedule implements LinkingDeviceManager.OnBatteryListener {
        OnBatteryListenerImpl(final LinkingDevice device) {
            super(device);
        }
    }
}
