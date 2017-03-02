/*
 TouchProfileConstants.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.profile;

/**
 * Constants for Touch Profile API.<br>
 * Define parameter name, interface name, attribute and profile name for touch
 * profile API.
 *
 * @deprecated swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 * @author NTT DOCOMO, INC.
 */
public interface TouchProfileConstants extends DConnectProfileConstants {

    /**
     * Profile name: {@value} .
     */
    String PROFILE_NAME = "touch";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_ON_TOUCH = "onTouch";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_ON_TOUCH_START = "onTouchStart";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_ON_TOUCH_END = "onTouchEnd";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_ON_DOUBLE_TAP = "onDoubleTap";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_ON_TOUCH_MOVE = "onTouchMove";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_ON_TOUCH_CANCEL = "onTouchCancel";

    /**
     * Path: {@value} .
     */
    String PATH_PROFILE = PATH_ROOT + SEPARATOR + PROFILE_NAME;

    /**
     * Path: {@value} .
     */
    String PATH_ON_TOUCH = PATH_PROFILE + SEPARATOR + ATTRIBUTE_ON_TOUCH;

    /**
     * Path: {@value} .
     */
    String PATH_ON_TOUCH_START = PATH_PROFILE + SEPARATOR + ATTRIBUTE_ON_TOUCH_START;

    /**
     * Path: {@value} .
     */
    String PATH_ON_TOUCH_END = PATH_PROFILE + SEPARATOR + ATTRIBUTE_ON_TOUCH_END;

    /**
     * Path: {@value} .
     */
    String PATH_ON_DOUBLE_TAP = PATH_PROFILE + SEPARATOR + ATTRIBUTE_ON_DOUBLE_TAP;

    /**
     * Path: {@value} .
     */
    String PATH_ON_TOUCH_MOVE = PATH_PROFILE + SEPARATOR + ATTRIBUTE_ON_TOUCH_MOVE;

    /**
     * Path: {@value} .
     */
    String PATH_ON_TOUCH_CANCEL = PATH_PROFILE + SEPARATOR + ATTRIBUTE_ON_TOUCH_CANCEL;

    /**
     * Parameter: {@value} .
     */
    String PARAM_TOUCH = "touch";

    /**
     * Parameter: {@value} .
     */
    String PARAM_TOUCHES = "touches";

    /**
     * Parameter: {@value} .
     */
    String PARAM_ID = "id";

    /**
     * Parameter: {@value} .
     */
    String PARAM_X = "x";

    /**
     * Parameter: {@value} .
     */
    String PARAM_Y = "y";

}
