/*
LightProfileConstants
Copyright (c) 2015 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.profile;

import org.deviceconnect.profile.DConnectProfileConstants;

/**
 * Light Profile API 定数群.<br/>
 * Light Profile API のパラメータ名、インタフェース名、属性名、プロファイル名を定義する。
 * 
 * @author NTT DOCOMO, INC.
 */
public interface LightProfileConstants extends DConnectProfileConstants {

    /**
     * プロファイル名: {@value} .
     */
    String PROFILE_NAME = "light";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_NAME = "name";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_LIGHTS = "lights";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_LIGHT_GROUPS = "lightGroups";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_LIGHT_ID = "lightId";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_ON = "on";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_CONFIG = "config";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_BRIGHTNESS = "brightness";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_COLOR = "color";

    /**
     * パラメータ: {@value}.
     */
    String PARAM_FLASHING = "flashing";

    /**
     * インターフェース: {@value} .
     */
    String INTERFACE_GROUP = "group";

    /**
     * インターフェース: {@value} .
     */
    String ATTRIBUTE_GROUP = "group";

    /**
     * 属性: {@value} .
     */
    String ATTRIBUTE_CREATE = "create";

    /**
     * 属性: {@value} .
     */
    String ATTRIBUTE_CLEAR = "clear";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_GROUP_ID = "groupId";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_LIGHT_IDS = "lightIds";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_GROUP_NAME = "groupName";
}
