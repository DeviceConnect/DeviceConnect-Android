/*
 LinkingVibrationProfile.java
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
import org.deviceconnect.android.profile.VibrationProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

public class LinkingVibrationProfile extends VibrationProfile {

    private VibrationExecutor mVibrationExecutor;

    public LinkingVibrationProfile() {
        addApi(mPutVibrate);
        addApi(mDeleteVibrate);
    }

    @Override
    protected long getMaxVibrationTime() {
        return 3 * 60 * 1000;
    }

    private DConnectApi mPutVibrate = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_VIBRATE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }

            long[] pattern = parsePattern(getPattern(request));
            LinkingDeviceManager manager = getLinkingDeviceManager();
            if (pattern != null) {
                patternVibrate(manager, device, pattern);
            } else {
                manager.sendVibrationCommand(device, true);
            }
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    private DConnectApi mDeleteVibrate = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_VIBRATE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }
            LinkingDeviceManager manager = getLinkingDeviceManager();
            manager.sendVibrationCommand(device, false);
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    private synchronized void patternVibrate(final LinkingDeviceManager manager,
                                             final LinkingDevice device, final long[] pattern) {
        if (mVibrationExecutor == null) {
            mVibrationExecutor = new VibrationExecutor();
        }
        mVibrationExecutor.setVibrationControllable((isOn, listener) -> {
            manager.sendVibrationCommand(device, isOn);
            listener.onComplete();
        });
        mVibrationExecutor.start(pattern);
    }

    private LinkingDevice getDevice(final Intent response) {
        LinkingDevice device = ((LinkingDeviceService) getService()).getLinkingDevice();

        if (!device.isConnected()) {
            MessageUtils.setIllegalDeviceStateError(response, "device not connected");
            return null;
        }

        if (!device.isSupportVibration()) {
            MessageUtils.setNotSupportProfileError(response, "device has not vibration");
            return null;
        }

        return device;
    }

    private LinkingDeviceManager getLinkingDeviceManager() {
        LinkingDevicePluginService service = (LinkingDevicePluginService) getContext();
        LinkingApplication app = (LinkingApplication) service.getApplication();
        return app.getLinkingDeviceManager();
    }
}
