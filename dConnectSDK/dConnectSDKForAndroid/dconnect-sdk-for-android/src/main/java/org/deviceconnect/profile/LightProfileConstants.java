/*
LightProfileConstants
Copyright (c) 2015 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.profile;

/**
 * Light Profile API 定数群.<br>
 * Light Profile API のパラメータ名、インタフェース名、属性名、プロファイル名を定義する。
 *
 * @deprecated swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
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
     * @deprecated 廃止します。
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
     * @deprecated 廃止します。
     */
    String INTERFACE_GROUP = "group";

    /**
     * インターフェース: {@value} .
     * @deprecated 廃止します。
     */
    String ATTRIBUTE_GROUP = "group";

    /**
     * 属性: {@value} .
     * @deprecated 廃止します。
     */
    String ATTRIBUTE_CREATE = "create";

    /**
     * 属性: {@value} .
     * @deprecated 廃止します。
     */
    String ATTRIBUTE_CLEAR = "clear";

    /**
     * パラメータ: {@value} .
     * @deprecated 廃止します。
     */
    String PARAM_GROUP_ID = "groupId";

    /**
     * パラメータ: {@value} .
     * @deprecated 廃止します。
     */
    String PARAM_LIGHT_IDS = "lightIds";

    /**
     * パラメータ: {@value} .
     * @deprecated 廃止します。
     */
    String PARAM_GROUP_NAME = "groupName";
}
