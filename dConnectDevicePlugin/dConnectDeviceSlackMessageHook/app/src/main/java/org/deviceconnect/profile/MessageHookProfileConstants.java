/*
 MessageHookProfileConstants.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.profile;

/**
 * Constants for Bot API.
 * @author NTT DOCOMO, INC.
 */
public interface MessageHookProfileConstants extends DConnectProfileConstants {

    /**
     * Profile name: {@value} .
     */
    String PROFILE_NAME = "messagehook";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_CHANNEL = "channel";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_MESSAGE = "message";

    /**
     * Parameter: {@value} .
     */
    String PARAM_CHANNEL = "channel";

    /**
     * Parameter: {@value} .
     */
    String PARAM_TEXT = "text";

    /**
     * Parameter: {@value} .
     */
    String PARAM_RESOURCE = "resource";

    /**
     * Parameter: {@value} .
     */
    String PARAM_MIME_TYPE = "mimeType";
}
