/*
 LinkingVibrationProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDevicePluginService;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.VibrationProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.HashMap;
import java.util.Map;

public class LinkingVibrationProfile extends VibrationProfile {

    private Map<String, VibrationExecutor> mVibrationMap = new HashMap<>();

    @Override
    protected boolean onPutVibrate(Intent request, Intent response, String serviceId, long[] pattern) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }
        LinkingDeviceManager manager = getLinkingDeviceManager();
        if (pattern != null) {
            patternVibrate(serviceId, manager, device, pattern);
        } else {
            manager.sendVibrationCommand(device, true);
        }
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onDeleteVibrate(Intent request, Intent response, String serviceId) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }
        LinkingDeviceManager manager = getLinkingDeviceManager();
        manager.sendVibrationCommand(device, false);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    private void patternVibrate(final String serviceId, final LinkingDeviceManager manager, final LinkingDevice device, final long[] pattern) {
        VibrationExecutor exe = mVibrationMap.get(serviceId);
        if (exe == null) {
            exe = new VibrationExecutor();
            mVibrationMap.put(serviceId, exe);
        }
        exe.setVibrationControllable(new VibrationExecutor.VibrationControllable() {
            @Override
            public void changeVibration(boolean isOn, VibrationExecutor.CompleteListener listener) {
                manager.sendVibrationCommand(device, isOn);
                listener.onComplete();
            }
        });
        exe.start(pattern);
    }

    private LinkingDevice getDevice(String serviceId, Intent response) {
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
        if (!device.isConnected()) {
            MessageUtils.setIllegalDeviceStateError(response, "device not connected");
            return null;
        }
        if (!LinkingUtil.hasVibration(device)) {
            MessageUtils.setNotSupportProfileError(response, "device has not vibration");
            return null;
        }
        return device;
    }

    @Override
    protected long getMaxVibrationTime() {
        return 3 * 60 * 1000;
    }

    private LinkingDeviceManager getLinkingDeviceManager() {
        LinkingDevicePluginService service = (LinkingDevicePluginService) getContext();
        LinkingApplication app = (LinkingApplication) service.getApplication();
        return app.getLinkingDeviceManager();
    }
}
