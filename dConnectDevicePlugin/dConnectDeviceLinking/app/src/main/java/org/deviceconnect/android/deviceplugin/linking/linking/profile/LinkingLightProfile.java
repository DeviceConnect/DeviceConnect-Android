/*
 LinkingLightProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDevicePluginService;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.deviceplugin.linking.linking.service.LinkingDeviceService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Light Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingLightProfile extends LightProfile {

    private FlashingExecutor mFlashingExecutor;

    public LinkingLightProfile() {
        addApi(mGetLightApi);
        addApi(mPostLightApi);
        addApi(mDeleteLightApi);
    }

    private final DConnectApi mGetLightApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }

            Bundle lightParam = new Bundle();
            setLightId(lightParam, device.getBdAddress());
            setName(lightParam, "Linking LED");
            setOn(lightParam, false);
            List<Bundle> lightParams = new ArrayList<>();
            lightParams.add(lightParam);
            setResult(response, DConnectMessage.RESULT_OK);
            setLights(response, lightParams);
            sendResponse(response);
            return true;
        }
    };

    private final DConnectApi mPostLightApi = new PostApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }

            String lightId = getLightId(request);
            if (lightId != null && !device.getBdAddress().equals(lightId)) {
                MessageUtils.setInvalidRequestParameterError(response, "lightId is invalid.");
                return true;
            }

            long[] flashing;
            try {
                flashing = getFlashing(request);
            } catch (Exception e) {
                MessageUtils.setInvalidRequestParameterError(response, "flashing is negative.");
                return true;
            }

            LinkingDeviceManager manager = getLinkingDeviceManager();
            if (flashing != null) {
                flashing(manager, device, flashing);
            } else {
                manager.sendLEDCommand(device, true);
            }
            setResult(response, DConnectMessage.RESULT_OK);
            sendResponse(response);
            return true;
        }
    };

    private final DConnectApi mDeleteLightApi = new DeleteApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }

            String lightId = getLightId(request);

            if (lightId != null && !device.getBdAddress().equals(lightId)) {
                MessageUtils.setInvalidRequestParameterError(response, "lightId is invalid.");
                return true;
            }

            getLinkingDeviceManager().sendLEDCommand(device, false);

            setResult(response, DConnectMessage.RESULT_OK);
            sendResponse(response);
            return true;
        }
    };

    private synchronized void flashing(final LinkingDeviceManager manager,
                                       final LinkingDevice device, final long[] flashing) {
        if (mFlashingExecutor == null) {
            mFlashingExecutor = new FlashingExecutor();
        }
        mFlashingExecutor.setLightControllable((isOn, listener) -> {
            manager.sendLEDCommand(device, isOn);
            listener.onComplete();
        });
        mFlashingExecutor.start(flashing);
    }

    private LinkingDevice getDevice(final Intent response) {
        LinkingDevice device = ((LinkingDeviceService) getService()).getLinkingDevice();

        if (!device.isConnected()) {
            MessageUtils.setIllegalDeviceStateError(response, "device not connected");
            return null;
        }

        if (!device.isSupportLED()) {
            MessageUtils.setIllegalDeviceStateError(response, "device has not LED");
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
