/*
 LinkingHumidityProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDevicePluginService;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.deviceplugin.linking.linking.service.LinkingDeviceService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.HumidityProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;

public class LinkingHumidityProfile extends HumidityProfile {

    public LinkingHumidityProfile() {
        addApi(mGetHumidity);
    }

    private final DConnectApi mGetHumidity = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }

            final LinkingDeviceManager deviceManager = getLinkingDeviceManager();
            deviceManager.enableListenHumidity(device, new OnHumidityListenerImpl(device) {
                @Override
                public void onCleanup() {
                    deviceManager.disableListenHumidity(mDevice, this);
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
                public void onHumidity(final LinkingDevice device, final float humidity) {
                    if (mCleanupFlag || !mDevice.equals(device)) {
                        return;
                    }

                    setHumidityToResponse(response, humidity);
                    sendResponse(response);
                    cleanup();
                }
            });
            return false;
        }
    };

    private void setHumidityToResponse(final Intent response, final float humidity) {
        setResult(response, DConnectMessage.RESULT_OK);
        setHumidity(response, humidity / 100.0f);
        setTimeStamp(response, System.currentTimeMillis());
    }

    private LinkingDevice getDevice(final Intent response) {
        LinkingDevice device = ((LinkingDeviceService) getService()).getLinkingDevice();

        if (!device.isConnected()) {
            MessageUtils.setIllegalDeviceStateError(response, "device not connected");
            return null;
        }

        if (!device.isSupportHumidity()) {
            MessageUtils.setIllegalDeviceStateError(response, "device has not humidity");
            return null;
        }

        return device;
    }

    private LinkingDeviceManager getLinkingDeviceManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingDeviceManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDevicePluginService service = (LinkingDevicePluginService) getContext();
        return (LinkingApplication) service.getApplication();
    }

    private abstract class OnHumidityListenerImpl extends TimeoutSchedule implements LinkingDeviceManager.OnHumidityListener {
        OnHumidityListenerImpl(final LinkingDevice device) {
            super(device);
        }
    }
}
