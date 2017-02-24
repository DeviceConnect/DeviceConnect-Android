/*
 KadecotTemperatureProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.KadecotHomeAirConditioner;
import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.KadecotResult;
import org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import static org.deviceconnect.android.deviceplugin.kadecot.profile.original.AirConditionerProfile.setTemperatureValue;
import static org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService.NO_RESULT;
import static org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService.createInvalidKadecotResponseError;

/**
 * Temperature Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotTemperatureProfile extends DConnectProfile {

    private static final int TYPE_CELSIUS = 1;
    private static final int TYPE_FAHRENHEIT = 2;

    public KadecotTemperatureProfile() {

        // GET /gotapi/temperature/
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                getTemperature(request, response);
                return false;
            }
        });

        // PUT /gotapi/temperature/
        addApi(new PutApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                putTemperature(request, response);
                return false;
            }
        });

    }

    @Override
    public String getProfileName() {
        return "temperature";
    }


    /**
     * Get air conditioner temperature value.
     *
     * @param request Request.
     * @param response Response.
     */
    private void getTemperature(final Intent request, final Intent response) {
        KadecotResult result = KadecotService.requestKadecotServer(getContext(), response, getServiceID(request),
                KadecotHomeAirConditioner.TEMPERATUREVALUE_GET);
        if (result != null) {
            String propertyName = result.getPropertyName();
            String propertyValue = result.getPropertyValue();
            if (propertyName != null && propertyValue != null) {
                if (propertyName.equals(KadecotHomeAirConditioner.PROP_SETTEMPERATUREVALUE)) {
                    setResult(response, DConnectMessage.RESULT_OK);
                    // Kadecot is only celsius.
                    int type = request.getIntExtra("type", TYPE_CELSIUS);
                    if (type == TYPE_FAHRENHEIT) {
                        propertyValue = "" + convertCelsiusToFahrenheit(new Integer(propertyValue));
                    }
                    response.putExtra("temperature", propertyValue);
                    response.putExtra("type", type);
                } else if (result.getServerResult().equals(NO_RESULT)) {
                    MessageUtils.setNotSupportAttributeError(response, "This device not support 'get' procedure.");
                } else {
                    createInvalidKadecotResponseError(response);
                }
            } else {
                createInvalidKadecotResponseError(response);
            }
        }
        sendResponse(response);
    }


    /**
     * Put air conditioner temperature value.
     *
     * @param request Request.
     * @param response Response.
     */
    private void putTemperature(final Intent request, final Intent response) {
        int value = -1;
        String strValue = request.getStringExtra("temperature");
        try {
            value = Integer.parseInt(strValue);
        } catch (NumberFormatException e) {
            value = -1;
        }
        if (value == -1 || value < 0 || value > 50) {
            MessageUtils.setInvalidRequestParameterError(response);
            sendResponse(response);
            return;
        }

        KadecotResult result = KadecotService.requestKadecotServer(getContext(),
                response, getServiceID(request),
                KadecotHomeAirConditioner.TEMPERATUREVALUE_SET, value);
        if (result != null) {
            String propertyName = result.getPropertyName();
            String propertyValue = result.getPropertyValue();
            if (propertyName != null && propertyValue != null) {
                if (propertyName.equals(KadecotHomeAirConditioner.PROP_SETTEMPERATUREVALUE)) {
                    if (Integer.parseInt(propertyValue) == value) {
                        setResult(response, DConnectMessage.RESULT_OK);
                    } else {
                        setResult(response, DConnectMessage.RESULT_ERROR);
                    }
                } else if (result.getServerResult().equals(NO_RESULT)) {
                    MessageUtils.setNotSupportAttributeError(response, "This device not support 'get' procedure.");
                } else {
                    KadecotService.createInvalidKadecotResponseError(response);
                }
            } else {
                KadecotService.createInvalidKadecotResponseError(response);
            }
        }
        sendResponse(response);
    }

    // Convert Celsius to Fahrenheit.
    private int convertCelsiusToFahrenheit(final int celsius) {
        return (int) (1.8 * celsius + 32);
    }

    // Convert Fahrenheit to Celsius.
    private int convertFahrenheitToCelsius(final int fahrenheit) {
        return (int) ((0.56) * (fahrenheit - 32));
    }
}