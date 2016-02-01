/*
 KeyEventProfileConstants.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.profile;

/**
 * Constants for Key Event Profile API.<br/>
 * Define parameter name, interface name, attribute and profile name for key
 * event profile API.
 * 
 * @author NTT DOCOMO, INC.
 */
public interface KeyEventProfileConstants extends DConnectProfileConstants {

    /**
     * Profile name: {@value} .
     */
    String PROFILE_NAME = "keyevent";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_KEYEVENT = "keyevent";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_ON_DOWN = "ondown";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_ON_UP = "onup";

    /**
     * Path: {@value} .
     */
    String PATH_PROFILE = PATH_ROOT + SEPARATOR + PROFILE_NAME;

    /**
     * Path: {@value} .
     */
    String PATH_ON_DOWN = PATH_PROFILE + SEPARATOR + ATTRIBUTE_ON_DOWN;

    /**
     * Path: {@value} .
     */
    String PATH_ON_UP = PATH_PROFILE + SEPARATOR + ATTRIBUTE_ON_UP;

    /**
     * Parameter: {@value} .
     */
    String PARAM_KEYEVENT = "keyevent";

    /**
     * Parameter: {@value} .
     */
    String PARAM_ID = "id";

    /**
     * Parameter: {@value} .
     */
    String PARAM_CONFIG = "config";

    /**
     * Parameter: {@value} .
     */
    int KEYTYPE_STD_KEY = 0x00000000;

    /**
     * Parameter: {@value} .
     */
    int KEYTYPE_MEDIA_CTRL = 0x00000200;

    /**
     * Parameter: {@value} .
     */
    int KEYTYPE_DPAD_BUTTON = 0x00000400;

    /**
     * Parameter: {@value} .
     */
    int KEYTYPE_USER = 0x00000800;

}
