/*
 LinkingLightProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManagerFactory;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Light Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingLightProfile extends LightProfile {

    @Override
    protected boolean onGetLight(final Intent request, final Intent response, final String serviceId) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }
        Bundle lightParam = new Bundle();
        setLightId(lightParam, device.getBdAddress());
        setName(lightParam, "Linking LED");
        setOn(lightParam, false);//liking device's status can not be take. So always OFF.
        List<Bundle> lightParams = new ArrayList<>();
        lightParams.add(lightParam);
        setLights(response, lightParams);
        sendResultOK(response);
        return true;
    }

    @Override
    protected boolean onPostLight(final Intent request, final Intent response, final String serviceId,
                                  String lightId, final Integer color, final Double brightness,
                                  final long[] flashing) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }
        LinkingManager manager = LinkingManagerFactory.createManager(getContext().getApplicationContext());
        manager.sendLEDCommand(device, true);
        sendResultOK(response);
        return true;
    }

    @Override
    protected boolean onDeleteLight(final Intent request, final Intent response, final String serviceId,
                                    String lightId) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }
        LinkingManager manager = LinkingManagerFactory.createManager(getContext().getApplicationContext());
        manager.sendLEDCommand(device, false);
        sendResultOK(response);
        return true;
    }

    private void sendResultOK(final Intent response) {
        setResult(response, DConnectMessage.RESULT_OK);
        ((DConnectMessageService) getContext()).sendResponse(response);
    }

    private void sendResultError(final Intent response) {
        MessageUtils.setUnknownError(response);
        ((DConnectMessageService) getContext()).sendResponse(response);
    }

    private void sendResultTimeout(final Intent response) {
        MessageUtils.setTimeoutError(response);
        ((DConnectMessageService) getContext()).sendResponse(response);
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
        if (!device.isConnected()) {
            MessageUtils.setIllegalDeviceStateError(response, "device not connected");
            return null;
        }
        if (!LinkingUtil.hasLED(device)) {
            MessageUtils.setIllegalDeviceStateError(response, "device has not LED");
            return null;
        }
        return device;
    }

}
