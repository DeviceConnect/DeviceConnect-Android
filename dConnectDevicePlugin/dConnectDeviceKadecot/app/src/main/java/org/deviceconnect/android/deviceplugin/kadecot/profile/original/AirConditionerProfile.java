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

import org.deviceconnect.android.profile.DConnectProfile;

/**
 * AirConditioner Profile.
 *
 * <p>
 * API that provides a air conditioner operation function.
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
public class AirConditionerProfile extends DConnectProfile implements AirConditionerProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
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
    public static void setPowerSaving(final Intent response, final String operationpowersaving) {
        response.putExtra(PARAM_POWERSAVING, operationpowersaving);
    }

    /**
     * Set operationmodesetting information to response message.
     *
     * @param response response message.
     * @param operationmodesetting operation mode setting information.
     */
    public static void setModeSetting(final Intent response, final String operationmodesetting) {
        response.putExtra(PARAM_MODESETTING, operationmodesetting);
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
    public static void setTemperature(final Intent response, final String temperaturevalue) {
        response.putExtra(PARAM_TEMPERATURE, temperaturevalue);
    }

    /**
     * Set airflowvalue information to response message.
     *
     * @param response response message.
     * @param airflowvalue air flow value.
     */
    public static void setAirFlow(final Intent response, final String airflowvalue) {
        response.putExtra(PARAM_AIRFLOW, airflowvalue);
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
        return request.getStringExtra(PARAM_POWERSAVING);
    }

    /**
     * Get operation mode setting parameter.
     *
     * @param request Request.
     * @return Operation mode setting parameter.
     */
    public static String getModeSetting(final Intent request) {
        return request.getStringExtra(PARAM_MODESETTING);
    }

    /**
     * Get temperature value parameter.
     *
     * @param request Request.
     * @return Temperature value parameter.
     */
    public static int getTemperature(final Intent request) {
        String strValue = request.getStringExtra(PARAM_TEMPERATURE);
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
    public static boolean isNullAirFlow(final Intent request) {
        return request.getStringExtra(PARAM_AIRFLOW) == null;
    }

    /**
     * Get air flow value parameter.
     *
     * @param request Request.
     * @return Air flow value parameter.
     */
    public static float getAirFlow(final Intent request) {
        String strValue = request.getStringExtra(PARAM_AIRFLOW);
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
