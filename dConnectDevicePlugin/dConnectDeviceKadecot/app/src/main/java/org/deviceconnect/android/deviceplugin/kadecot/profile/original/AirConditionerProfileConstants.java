/*
 AirConditionerProfileConstants.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.profile.original;

import org.deviceconnect.profile.DConnectProfileConstants;


/**
 * Constants for AirConditioner Profile API.<br/>
 * Define parameter name, interface name, attribute and profile name for
 * AirConditioner profile API.
 *
 * @author NTT DOCOMO, INC.
 */
public interface AirConditionerProfileConstants extends DConnectProfileConstants {

    /**
     * Profile name: {@value} .
     */
    String PROFILE_NAME = "airconditioner";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_AIR_CONDITIONER = "airconditioner";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_OPERATION_POWER_SAVING = "operationpowersaving";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_OPERATION_MODE_SETTING = "operationmodesetting";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_ROOM_TEMPERATURE = "roomtemperature";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_TEMPERATURE_VALUE = "temperaturevalue";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_AIR_FLOW_VALUE = "airflowvalue";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_ENL_PROPERTY = "enlproperty";

    /**
     * Path: {@value} .
     */
    String PATH_PROFILE = PATH_ROOT + SEPARATOR + PROFILE_NAME;

    /**
     * Path: {@value} .
     */
    String PATH_OPERATION_POWER_SAVING = PATH_PROFILE + SEPARATOR + ATTRIBUTE_OPERATION_POWER_SAVING;

    /**
     * Path: {@value} .
     */
    String PATH_OPERATION_MODE_SETTING = PATH_PROFILE + SEPARATOR + ATTRIBUTE_OPERATION_MODE_SETTING;

    /**
     * Path: {@value} .
     */
    String PATH_ROOM_TEMPERATURE = PATH_PROFILE + SEPARATOR + ATTRIBUTE_ROOM_TEMPERATURE;

    /**
     * Path: {@value} .
     */
    String PATH_TEMPERATURE_VALUE = PATH_PROFILE + SEPARATOR + ATTRIBUTE_TEMPERATURE_VALUE;

    /**
     * Path: {@value} .
     */
    String PATH_AIR_FLOW_VALUE = PATH_PROFILE + SEPARATOR + ATTRIBUTE_AIR_FLOW_VALUE;

    /**
     * Path: {@value} .
     */
    String PATH_ENL_PROPERTY = PATH_PROFILE + SEPARATOR + ATTRIBUTE_ENL_PROPERTY;

    /**
     * Parameter: {@value} .
     */
    String PARAM_POWERSTATUS = "powerstatus";

    /**
     * Parameter: {@value} .
     */
    String PARAM_OPERATIONPOWERSAVING = "operationpowersaving";

    /**
     * Parameter: {@value} .
     */
    String PARAM_OPERATIONMODESETTING = "operationmodesetting";

    /**
     * Parameter: {@value} .
     */
    String PARAM_ROOMTEMPERATURE = "roomtemperature";

    /**
     * Parameter: {@value} .
     */
    String PARAM_TEMPERATUREVALUE = "temperaturevalue";

    /**
     * Parameter: {@value} .
     */
    String PARAM_AIRFLOWVALUE = "airflowvalue";

    /**
     * Parameter: {@value} .
     */
    String PARAM_AIRFLOWAUTO = "airflowauto";

    /**
     * Parameter: {@value} .
     */
    String PARAM_PROPERTIES = "properties";

    /**
     * Parameter: {@value} .
     */
    String PARAM_EPC = "epc";

    /**
     * Parameter: {@value} .
     */
    String PARAM_VALUE = "value";

}
