/*
 LinkingTemperatureProfile.java
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
import org.deviceconnect.android.profile.TemperatureProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;

public class LinkingTemperatureProfile extends TemperatureProfile {

    public LinkingTemperatureProfile() {
        addApi(mGetTemperature);
    }

    private final DConnectApi mGetTemperature = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }

            final LinkingDeviceManager deviceManager = getLinkingDeviceManager();
            deviceManager.enableListenTemperature(device, new OnTemperatureListenerImpl(device) {
                @Override
                public void onCleanup() {
                    deviceManager.disableListenTemperature(mDevice, this);
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
                public void onTemperature(final LinkingDevice device, final float temperature) {
                    if (mCleanupFlag || !mDevice.equals(device)) {
                        return;
                    }

                    setTemperatureToResponse(request, response, temperature);
                    sendResponse(response);
                    cleanup();
                }
            });
            return false;
        }
    };

    private void setTemperatureToResponse(final Intent request, final Intent response, final float temperature) {
        int type = TemperatureProfile.getType(request);
        TemperatureType tType = TemperatureType.TYPE_CELSIUS;
        float temp = temperature;
        if (type == TemperatureType.TYPE_FAHRENHEIT.getValue()) {
            temp = TemperatureProfile.convertCelsiusToFahrenheit((int) temp);
            tType = TemperatureType.TYPE_FAHRENHEIT;
        }
        setResult(response, DConnectMessage.RESULT_OK);
        setTemperature(response, temp);
        setTemperatureType(response, tType);
        setTimeStamp(response, System.currentTimeMillis());
    }

    private LinkingDeviceManager getLinkingDeviceManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingDeviceManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDevicePluginService service = (LinkingDevicePluginService) getContext();
        return (LinkingApplication) service.getApplication();
    }

    private LinkingDevice getDevice(final Intent response) {
        LinkingDevice device = ((LinkingDeviceService) getService()).getLinkingDevice();

        if (!device.isConnected()) {
            MessageUtils.setIllegalDeviceStateError(response, "device not connected");
            return null;
        }

        if (!device.isSupportTemperature()) {
            MessageUtils.setIllegalDeviceStateError(response, "device has not temperature");
            return null;
        }

        return device;
    }

    private abstract class OnTemperatureListenerImpl extends TimeoutSchedule implements LinkingDeviceManager.OnTemperatureListener {
        OnTemperatureListenerImpl(final LinkingDevice device) {
            super(device);
        }
    }
}
