/*
 FPLUGLightProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.fplug.FPLUGApplication;
import org.deviceconnect.android.deviceplugin.fplug.FPLUGDeviceService;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGController;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGRequestCallback;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGResponse;
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
public class FPLUGLightProfile extends LightProfile {

    @Override
    protected boolean onGetLight(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null || serviceId.length() == 0) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        FPLUGApplication app = ((FPLUGApplication) getContext().getApplicationContext());
        List<FPLUGController> fplugs = app.getConnectedController();
        FPLUGController fplug = app.getConnectedController(serviceId);
        if (fplugs == null) {
            MessageUtils.setNotFoundServiceError(response, "Not found fplug: " + serviceId);
            return true;
        }

        Bundle lightParam = new Bundle();
        setLightId(lightParam, fplug.getAddress());
        setName(lightParam, "F-PLUG LED");
        setConfig(lightParam, "");
        setOn(lightParam, false);//f-plug's status can not be take. So always OFF.
        List<Bundle> lightParams = new ArrayList<>();
        lightParams.add(lightParam);
        setLights(response, lightParams);

        sendResultOK(response);
        return true;
    }

    @Override
    protected boolean onPostLight(final Intent request, final Intent response, final String serviceId,
                                  final String lightId, final Integer color, final Double brightness,
                                  final long[] flashing) {
        if (serviceId == null || serviceId.length() == 0) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        if (lightId == null || lightId.length() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
            return true;
        }

        FPLUGApplication app = ((FPLUGApplication) getContext().getApplicationContext());
        FPLUGController controller = app.getConnectedController(lightId);
        if (controller == null) {
            MessageUtils.setInvalidRequestParameterError(response, "Not found fplug: " + lightId);
            return true;
        }
        controller.requestLEDControl(true, new FPLUGRequestCallback() {
            @Override
            public void onSuccess(final FPLUGResponse fResponse) {
                sendResultOK(response);
            }

            @Override
            public void onError(final String message) {
                sendResultError(response);
            }

            @Override
            public void onTimeout() {
                sendResultTimeout(response);
            }
        });
        return false;
    }

    @Override
    protected boolean onDeleteLight(final Intent request, final Intent response, final String serviceId,
                                    final String lightId) {
        if (serviceId == null || serviceId.length() == 0) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        // 必須パラメータの存在チェック
        if (lightId == null || lightId.length() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
            return true;
        }
        FPLUGApplication app = ((FPLUGApplication) getContext().getApplicationContext());
        FPLUGController controller = app.getConnectedController(lightId);
        if (controller == null) {
            MessageUtils.setInvalidRequestParameterError(response, "Not found fplug: " + lightId);
            return true;
        }
        controller.requestLEDControl(false, new FPLUGRequestCallback() {
            @Override
            public void onSuccess(final FPLUGResponse fResponse) {
                sendResultOK(response);
            }

            @Override
            public void onError(final String message) {
                sendResultError(response);
            }

            @Override
            public void onTimeout() {
                sendResultTimeout(response);
            }
        });
        return false;
    }

    private void sendResultOK(final Intent response) {
        setResult(response, DConnectMessage.RESULT_OK);
        ((FPLUGDeviceService) getContext()).sendResponse(response);
    }

    private void sendResultError(final Intent response) {
        MessageUtils.setUnknownError(response);
        ((FPLUGDeviceService) getContext()).sendResponse(response);
    }

    private void sendResultTimeout(final Intent response) {
        MessageUtils.setTimeoutError(response);
        ((FPLUGDeviceService) getContext()).sendResponse(response);
    }
}
