/*
 LinkingVibrationProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking.profile;

import android.content.Intent;
import android.util.Log;

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

import java.util.HashMap;
import java.util.Map;

public class LinkingVibrationProfile extends VibrationProfile {

    private Map<String, VibrationExecutor> mVibrationMap = new HashMap<>();

    public LinkingVibrationProfile() {
        addApi(mPutVibrate);
        addApi(mDeleteVibrate);
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

    private void patternVibrate(final LinkingDeviceManager manager,
                                final LinkingDevice device, final long[] pattern) {
        VibrationExecutor exe = mVibrationMap.get("test");
        if (exe == null) {
            exe = new VibrationExecutor();
            mVibrationMap.put("test", exe);
        }

        exe.setVibrationControllable(new VibrationExecutor.VibrationControllable() {
            @Override
            public void changeVibration(final boolean isOn, final VibrationExecutor.CompleteListener listener) {
                manager.sendVibrationCommand(device, isOn);
                listener.onComplete();
            }
        });
        exe.start(pattern);
    }

    private LinkingDevice getDevice(final Intent response) {
        LinkingDevice device = ((LinkingDeviceService) getService()).getLinkingDevice();

        if (!device.isConnected()) {
            MessageUtils.setIllegalDeviceStateError(response, "device not connected");
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
