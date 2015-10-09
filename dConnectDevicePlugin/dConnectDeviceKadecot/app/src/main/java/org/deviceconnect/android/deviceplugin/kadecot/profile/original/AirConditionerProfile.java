/*
 AirConditionerProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.profile.original;
/*
 AirConditionerProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;

/**
 * AirConditioner Profile.
 *
 * <p>
 * API that provides a air conditioner operation function.<br/>
 * Device plug-in that provides a air conditioner operation function by extending this
 * class, and implements the corresponding API that.<br/>
 * </p>
 *
 * <h1>API provides methods</h1>
 * <p>
 * The request to each API of AirConditioner Profile, following callback method group is
 * automatically invoked.<br/>
 * Subclass is to implement the functionality by overriding the method for the
 * API provided by the device plug-in from the following methods group.<br/>
 * Features that are not overridden automatically return the response as
 * non-compliant API.
 * </p>
 * <ul>
 * <li>AirConditioner Get Power Status API [GET] :
 * {@link AirConditionerProfile#onGetAirConditioner(Intent, Intent, String)}</li>
 * <li>AirConditioner Power On API [PUT] :
 * {@link AirConditionerProfile#onPutAirConditioner(Intent, Intent, String)}</li>
 * <li>AirConditioner Power Off API [DELETE] :
 * {@link AirConditionerProfile#onDeleteAirConditioner(Intent, Intent, String)}</li>
 * <li>AirConditioner Get Operation Power Saving API [GET] :
 * {@link AirConditionerProfile#onGetAirConditionerOperationPowerSaving(Intent, Intent, String)}</li>
 * <li>AirConditioner Set Operation Power Saving API [PUT] :
 * {@link AirConditionerProfile#onPutAirConditionerOperationPowerSaving(Intent, Intent, String)}</li>
 * <li>AirConditioner Get Operation Mode Setting API [GET] :
 * {@link AirConditionerProfile#onGetAirConditionerOperationModeSetting(Intent, Intent, String)}</li>
 * <li>AirConditioner Set Operation Mode Setting API [PUT] :
 * {@link AirConditionerProfile#onPutAirConditionerOperationModeSetting(Intent, Intent, String)}</li>
 * <li>AirConditioner Get Room Temperature API [GET] :
 * {@link AirConditionerProfile#onGetAirConditionerRoomTemperature(Intent, Intent, String)}</li>
 * <li>AirConditioner Get Temperature Value API [GET] :
 * {@link AirConditionerProfile#onGetAirConditionerTemperatureValue(Intent, Intent, String)}</li>
 * <li>AirConditioner Set Temperature Value API [PUT] :
 * {@link AirConditionerProfile#onPutAirConditionerTemperatureValue(Intent, Intent, String)}</li>
 * <li>AirConditioner Get Air Flow Value API [GET] :
 * {@link AirConditionerProfile#onGetAirConditionerAirFlowValue(Intent, Intent, String)}</li>
 * <li>AirConditioner Set Air Flow Value API [PUT] :
 * {@link AirConditionerProfile#onPutAirConditionerAirFlowValue(Intent, Intent, String)}</li>
 * <li>AirConditioner Get ECHONET Lite  Property API [GET] :
 * {@link AirConditionerProfile#onGetAirConditionerECHONETLiteProperty(Intent, Intent, String)}</li>
 * <li>AirConditioner Set ECHONET Lite Property API [PUT] :
 * {@link AirConditionerProfile#onPutAirConditionerECHONETLiteProperty(Intent, Intent, String)}</li>
 * </ul>
 *
 * @author NTT DOCOMO, INC.
 */
public class AirConditionerProfile extends DConnectProfile implements AirConditionerProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        boolean result = true;
        String attribute = getAttribute(request);

        if (ATTRIBUTE_OPERATION_POWER_SAVING.equals(attribute)) {
            result = onGetAirConditionerOperationPowerSaving(request, response, getServiceID(request));
        } else if (ATTRIBUTE_OPERATION_MODE_SETTING.equals(attribute)) {
            result = onGetAirConditionerOperationModeSetting(request, response, getServiceID(request));
        } else if (ATTRIBUTE_ROOM_TEMPERATURE.equals(attribute)) {
            result = onGetAirConditionerRoomTemperature(request, response, getServiceID(request));
        } else if (ATTRIBUTE_TEMPERATURE_VALUE.equals(attribute)) {
            result = onGetAirConditionerTemperatureValue(request, response, getServiceID(request));
        } else if (ATTRIBUTE_AIR_FLOW_VALUE.equals(attribute)) {
            result = onGetAirConditionerAirFlowValue(request, response, getServiceID(request));
        } else if (ATTRIBUTE_ENL_PROPERTY.equals(attribute)) {
            result = onGetAirConditionerECHONETLiteProperty(request, response, getServiceID(request));
        } else if (attribute == null) {
            result = onGetAirConditioner(request, response, getServiceID(request));
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }

    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {
        boolean result = true;
        String attribute = getAttribute(request);

        if (ATTRIBUTE_OPERATION_POWER_SAVING.equals(attribute)) {
            result = onPutAirConditionerOperationPowerSaving(request, response, getServiceID(request));
        } else if (ATTRIBUTE_OPERATION_MODE_SETTING.equals(attribute)) {
            result = onPutAirConditionerOperationModeSetting(request, response, getServiceID(request));
        } else if (ATTRIBUTE_TEMPERATURE_VALUE.equals(attribute)) {
            result = onPutAirConditionerTemperatureValue(request, response, getServiceID(request));
        } else if (ATTRIBUTE_AIR_FLOW_VALUE.equals(attribute)) {
            result = onPutAirConditionerAirFlowValue(request, response, getServiceID(request));
        } else if (ATTRIBUTE_ENL_PROPERTY.equals(attribute)) {
            result = onPutAirConditionerECHONETLiteProperty(request, response, getServiceID(request));
        } else if (attribute == null) {
            result = onPutAirConditioner(request, response, getServiceID(request));
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }

    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {
        boolean result = true;
        String attribute = getAttribute(request);

        if (attribute == null) {
            result = onDeleteAirConditioner(request, response, getServiceID(request));
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }

    // ------------------------------------
    // GET
    // ------------------------------------
    /**
     * AirConditioner get request handler.<br/>
     * Get the AirConditioner result and store in the response parameter.
     * If you have ready to transmit the response parameter that you
     * specify the true return value. If you are not ready to be submitted
     * response parameters, be false for the return value. Then, in the thread
     * to launch the threads eventually doing the transmission of response
     * parameters.
     *
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onGetAirConditioner(final Intent request, final Intent response, final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * AirConditionerOperationPowerSaving get request handler.<br/>
     * Get the AirConditionerOperationPowerSaving result and store in the response parameter.
     * If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     *
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onGetAirConditionerOperationPowerSaving(final Intent request, final Intent response,
                                                              final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * AirConditionerOperationModeSetting get request handler.<br/>
     * Get the AirConditionerOperationModeSetting result and store in the response parameter.
     * If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     *
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onGetAirConditionerOperationModeSetting(final Intent request, final Intent response,
                                                              final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * AirConditionerRoomTemperature get request handler.<br/>
     * Get the AirConditionerRoomTemperature result and store in the response parameter.
     * If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     *
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onGetAirConditionerRoomTemperature(final Intent request, final Intent response,
                                                         final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * AirConditionerTemperatureValue get request handler.<br/>
     * Get the AirConditionerTemperatureValue result and store in the response parameter.
     * If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     *
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onGetAirConditionerTemperatureValue(final Intent request, final Intent response,
                                                          final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * AirConditionerAirFlowValue get request handler.<br/>
     * Get the AirConditionerAirFlowValue result and store in the response parameter.
     * If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     *
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onGetAirConditionerAirFlowValue(final Intent request, final Intent response,
                                                      final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * AirConditionerECHONETLiteProperty get request handler.<br/>
     * Get the AirConditionerECHONETLiteProperty result and store in the response parameter.
     * If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     *
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onGetAirConditionerECHONETLiteProperty(final Intent request, final Intent response,
                                                             final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    // ------------------------------------
    // PUT
    // ------------------------------------

    /**
     * AirConditioner put request handler.<br/>
     * Put the AirConditioner result and store in the response parameter.
     * If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     *
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onPutAirConditioner(final Intent request, final Intent response, final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * AirConditionerOperationPowerSaving put request handler.<br/>
     * Put the AirConditionerOperationPowerSaving result and store in the response parameter.
     * If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     *
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onPutAirConditionerOperationPowerSaving(final Intent request, final Intent response,
                                                              final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * AirConditionerOperationModeSetting put request handler.<br/>
     * Put the AirConditionerOperationModeSetting result and store in the response parameter.
     * If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     *
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onPutAirConditionerOperationModeSetting(final Intent request, final Intent response,
                                                              final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * AirConditionerTemperatureValue put request handler.<br/>
     * Put the AirConditionerTemperatureValue result and store in the response parameter.
     * If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     *
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onPutAirConditionerTemperatureValue(final Intent request, final Intent response,
                                                          final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * AirConditionerAirFlowValue put request handler.<br/>
     * Put the AirConditionerAirFlowValue result and store in the response parameter.
     * If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     *
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onPutAirConditionerAirFlowValue(final Intent request, final Intent response,
                                                      final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * AirConditionerECHONETLiteProperty put request handler.<br/>
     * Put the AirConditionerECHONETLiteProperty result and store in the response parameter.
     * If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     *
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onPutAirConditionerECHONETLiteProperty(final Intent request, final Intent response,
                                                             final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    // ------------------------------------
    // DELETE
    // ------------------------------------

    /**
     * AirConditioner delete request handler.<br/>
     * Delete the AirConditioner result and store in the response parameter.
     * If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     *
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onDeleteAirConditioner(final Intent request, final Intent response, final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    // ------------------------------------
    // Message setter method group
    // ------------------------------------

    /**
     * Set powerstatus information to airconditioner object.
     *
     * @param response response message.
     * @param powerstatus power status information.
     */
    public static void setPowerStatus(final Intent response, final String powerstatus) {
        response.putExtra(PARAM_POWERSTATUS, powerstatus);
    }

    /**
     * Set operationpowersaving information to response message.
     *
     * @param response response message.
     * @param operationpowersaving operation power saving information.
     */
    public static void setOperationPowerSaving(final Intent response, final String operationpowersaving) {
        response.putExtra(PARAM_OPERATIONPOWERSAVING, operationpowersaving);
    }

    /**
     * Set operationmodesetting information to response message.
     *
     * @param response response message.
     * @param operationmodesetting operation mode setting information.
     */
    public static void setOperationModeSetting(final Intent response, final String operationmodesetting) {
        response.putExtra(PARAM_OPERATIONMODESETTING, operationmodesetting);
    }

    /**
     * Set roomtemperature information to response message.
     *
     * @param response response message.
     * @param roomtemperature room temperature.
     */
    public static void setRoomTemperature(final Intent response, final String roomtemperature) {
        response.putExtra(PARAM_ROOMTEMPERATURE, roomtemperature);
    }

    /**
     * Set temperaturevalue information to response message.
     *
     * @param response response message.
     * @param temperaturevalue temperature value.
     */
    public static void setTemperatureValue(final Intent response, final String temperaturevalue) {
        response.putExtra(PARAM_TEMPERATUREVALUE, temperaturevalue);
    }

    /**
     * Set airflowvalue information to response message.
     *
     * @param response response message.
     * @param airflowvalue air flow value.
     */
    public static void setAirFlowValue(final Intent response, final String airflowvalue) {
        response.putExtra(PARAM_AIRFLOWVALUE, airflowvalue);
    }

    /**
     * Set airflowauto information to response message.
     *
     * @param response response message.
     * @param airflowauto air flow auto value.
     */
    public static void setAirFlowAuto(final Intent response, final String airflowauto) {
        response.putExtra(PARAM_AIRFLOWAUTO, airflowauto);
    }

    /**
     * Set properties information to response message.
     *
     * @param response response message.
     * @param properties properties.
     */
    public static void setProperties(final Intent response, final String properties) {
        response.putExtra(PARAM_PROPERTIES, properties);
    }

    /**
     * Set epc information to response message.
     *
     * @param response response message.
     * @param epc epc.
     */
    public static void setEpc(final Intent response, final String epc) {
        response.putExtra(PARAM_EPC, epc);
    }

    /**
     * Set value information to response message.
     *
     * @param response response message.
     * @param value value.
     */
    public static void setValue(final Intent response, final String value) {
        response.putExtra(PARAM_VALUE, value);
    }

    // ------------------------------------
    // Message getter method group
    // ------------------------------------

    /**
     * Get power saving parameter.
     *
     * @param request Request.
     * @return Power saving parameter.
     */
    public static String getPowerSaving(final Intent request) {
        return request.getStringExtra(PARAM_OPERATIONPOWERSAVING);
    }

    /**
     * Get operation mode setting parameter.
     *
     * @param request Request.
     * @return Operation mode setting parameter.
     */
    public static String getOperationModeSetting(final Intent request) {
        return request.getStringExtra(PARAM_OPERATIONMODESETTING);
    }

    /**
     * Get temperature value parameter.
     *
     * @param request Request.
     * @return Temperature value parameter.
     */
    public static int getTemperatureValue(final Intent request) {
        String strValue = request.getStringExtra(PARAM_TEMPERATUREVALUE);
        try {
            return Integer.parseInt(strValue);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Check Air Flow Value parameter.
     *
     * @param request Request.
     * @return true : Not found. / false : Found parameter.
     */
    public static boolean isNullAirFlowValue(final Intent request) {
        return request.getStringExtra(PARAM_AIRFLOWVALUE) == null;
    }

    /**
     * Get air flow value parameter.
     *
     * @param request Request.
     * @return Air flow value parameter.
     */
    public static float getAirFlowValue(final Intent request) {
        String strValue = request.getStringExtra(PARAM_AIRFLOWVALUE);
        if (strValue == null) {
            return -1;
        }

        try {
            return Float.parseFloat(strValue);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Check Air Flow Auto parameter.
     *
     * @param request Request.
     * @return true : Not found. / false : Found parameter.
     */
    public static boolean isNullAirFlowAuto(final Intent request) {
        return request.getStringExtra(PARAM_AIRFLOWAUTO) == null;
    }

    /**
     * Get Air Flow Auto parameter.
     *
     * @param request Request.
     * @return Air flow auto parameter.
     */
    public static boolean getAirFlowAuto(final Intent request) {
        String auto = request.getStringExtra(PARAM_AIRFLOWAUTO);
        if (auto == null) {
            return false;
        }

        switch (auto) {
            case "true":
            case "True":
            case "TRUE":    return true;
            default:        return false;
        }
    }

    /**
     * Get epc parameter.
     *
     * @param request Request.
     * @return Epc parameter.
     */
    public static String getEpc(final Intent request) {
        return request.getStringExtra(PARAM_EPC);
    }

    /**
     * Get epc value parameter.
     *
     * @param request Request.
     * @return Epc value parameter.
     */
    public static String getEpcValue(final Intent request) {
        return request.getStringExtra(PARAM_VALUE);
    }

}
