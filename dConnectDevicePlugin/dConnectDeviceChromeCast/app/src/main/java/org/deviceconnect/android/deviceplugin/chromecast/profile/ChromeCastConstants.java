/*
 ChromeCastConstants.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.profile;

/**
 * Constants for ChromeCast Receiver App Interface.
 * @author NTT DOCOMO, INC.
 */
interface ChromeCastConstants {

    /** Parameter key: {@value}. */
    String KEY_FUNCTION = "function";

    /** Parameter key: {@value}. */
    String KEY_MODE = "mode";

    /** Parameter key: {@value}. */
    String KEY_URL = "url";

    /** Parameter key: {@value}. */
    String KEY_X = "x";

    /** Parameter key: {@value}. */
    String KEY_Y = "y";

    /** Parameter key: {@value}. */
    String KEY_TYPE = "type";

    /** Parameter key: {@value}. */
    String KEY_MESSAGE = "message";

    /** Function type: {@value}. */
    String FUNCTION_POST_NOTIFICATION = "write";

    /** Function type: {@value}. */
    String FUNCTION_DELETE_NOTIFICATION = "clear";

    /** Function type: {@value}. */
    String FUNCTION_POST_IMAGE = "canvas_draw";

    /** Function type: {@value}. */
    String FUNCTION_DELETE_IMAGE = "canvas_delete";

}
