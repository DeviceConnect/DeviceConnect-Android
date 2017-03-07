/*
 FPLUGTemperatureProfile.java
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
import org.deviceconnect.android.profile.TemperatureProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * Temperature Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGTemperatureProfile extends TemperatureProfile {

    private final DConnectApi mGetTemperatureApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);

            FPLUGApplication app = ((FPLUGApplication) getContext().getApplicationContext());
            FPLUGController controller = app.getConnectedController(serviceId);
            if (controller == null) {
                MessageUtils.setNotFoundServiceError(response, "Not found fplug: " + serviceId);
                return true;
            }
            controller.requestTemperature(new FPLUGRequestCallback() {
                @Override
                public void onSuccess(FPLUGResponse fResponse) {
                    double temp = fResponse.getTemperature();
                    String typeString = request.getStringExtra("type");
                    int type;
                    try {
                        type = Integer.valueOf(typeString);
                    } catch(NumberFormatException e) {
                        type = TemperatureType.Celsius.getValue();
                    }
                    if (type == TemperatureType.CelsiusFahrenheit.getValue()) {
                        temp = convertCelsiusToFahrenheit(temp);
                    }
                    setTemperature(response, temp);
                    setType(response, type);
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

    public FPLUGTemperatureProfile() {
        addApi(mGetTemperatureApi);
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
    // Convert Celsius to Fahrenheit.
    private double convertCelsiusToFahrenheit(final double celsius) {
        return (1.8 * celsius + 32);
    }

    // Convert Fahrenheit to Celsius.
    private double convertFahrenheitToCelsius(final double fahrenheit) {
        return ((0.56) * (fahrenheit - 32));
    }
}
