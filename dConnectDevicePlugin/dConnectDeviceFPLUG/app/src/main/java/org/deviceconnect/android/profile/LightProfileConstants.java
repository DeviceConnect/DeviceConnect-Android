/*
 LightProfileConstants.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.profile;

import org.deviceconnect.profile.DConnectProfileConstants;

/**
 * Constants for Humidity profile.
 *
 * @author NTT DOCOMO, INC.
 */
public interface LightProfileConstants extends DConnectProfileConstants {

    /**
     * Profile name: {@value} .
     */
    String PROFILE_NAME = "light";

    /**
     * Parameter: {@value} .
     */
    String PARAM_NAME = "name";

    /**
     * Parameter: {@value} .
     */
    String PARAM_LIGHTS = "lights";

    /**
     * Parameter: {@value} .
     */
    String PARAM_LIGHT_GROUPS = "lightGroups";

    /**
     * Parameter: {@value} .
     */
    String PARAM_LIGHT_ID = "lightId";

    /**
     * Parameter: {@value} .
     */
    String PARAM_ON = "on";

    /**
     * Parameter: {@value} .
     */
    String PARAM_CONFIG = "config";

    /**
     * Parameter: {@value} .
     */
    String PARAM_BRIGHTNESS = "brightness";

    /**
     * Parameter: {@value} .
     */
    String PARAM_COLOR = "color";

    /**
     * Interface: {@value} .
     */
    String INTERFACE_GROUP = "group";

    /**
     * Interface: {@value} .
     */
    String ATTRIBUTE_GROUP = "group";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_CREATE = "create";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_CLEAR = "clear";

    /**
     * Parameter: {@value} .
     */
    String PARAM_GROUP_ID = "groupId";

    /**
     * Parameter: {@value} .
     */
    String PARAM_LIGHT_IDS = "lightIds";

    /**
     * Parameter: {@value} .
     */
    String PARAM_GROUP_NAME = "groupName";
}
