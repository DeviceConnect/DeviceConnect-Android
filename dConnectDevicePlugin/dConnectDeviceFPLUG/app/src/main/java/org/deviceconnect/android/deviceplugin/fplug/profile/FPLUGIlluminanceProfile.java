/*
 FPLUGIlluminanceProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.fplug.FPLUGApplication;
import org.deviceconnect.android.deviceplugin.fplug.FPLUGDeviceService;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGController;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGRequestCallback;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGResponse;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.IlluminanceProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * Illuminance Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGIlluminanceProfile extends IlluminanceProfile {

    private final DConnectApi mGetIlluminanceApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);

            FPLUGApplication app = ((FPLUGApplication) getContext().getApplicationContext());
            FPLUGController controller = app.getConnectedController(serviceId);
            if (controller == null) {
                MessageUtils.setNotFoundServiceError(response, "Not found fplug: " + serviceId);
                return true;
            }
            controller.requestIlluminance(new FPLUGRequestCallback() {
                @Override
                public void onSuccess(FPLUGResponse fResponse) {
                    double illuminance = fResponse.getIlluminance();
                    setIlluminance(response, illuminance);
                    sendResultOK(response);
                }

                @Override
                public void onError(String message) {
                    sendResultError(response);
                }

                @Override
                public void onTimeout() {
                    sendResultTimeout(response);
                }
            });
            return false;
        }
    };

    public FPLUGIlluminanceProfile() {
        addApi(mGetIlluminanceApi);
    }

    private void sendResultOK(Intent response) {
        setResult(response, DConnectMessage.RESULT_OK);
        ((FPLUGDeviceService) getContext()).sendResponse(response);
    }

    private void sendResultError(Intent response) {
        MessageUtils.setUnknownError(response);
        ((FPLUGDeviceService) getContext()).sendResponse(response);
    }

    private void sendResultTimeout(Intent response) {
        MessageUtils.setTimeoutError(response);
        ((FPLUGDeviceService) getContext()).sendResponse(response);
    }

}
