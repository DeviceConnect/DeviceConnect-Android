/*
 LinkingVibrationProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManagerFactory;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.VibrationProfile;
import org.deviceconnect.message.DConnectMessage;

public class LinkingVibrationProfile extends VibrationProfile {

    @Override
    protected boolean onPutVibrate(Intent request, Intent response, String serviceId, long[] pattern) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }
        LinkingManager manager = LinkingManagerFactory.createManager(getContext().getApplicationContext());
        manager.sendVibrationCommand(device, true);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onDeleteVibrate(Intent request, Intent response, String serviceId) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }
        LinkingManager manager = LinkingManagerFactory.createManager(getContext().getApplicationContext());
        manager.sendVibrationCommand(device, false);
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
        if (!device.isConnected()) {
            MessageUtils.setIllegalDeviceStateError(response, "device not connected");
            return null;
        }
        if (!LinkingUtil.hasVibration(device)) {
            MessageUtils.setIllegalDeviceStateError(response, "device has not vibration");
            return null;
        }
        return device;
    }

}
