/*
 SystemProfileConstants.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.profile;

/**
 * System Profile API 定数群.<br/>
 * System Profile API のパラメータ名、インタフェース名、属性名、プロファイル名を定義する。
 * @author NTT DOCOMO, INC.
 */
public interface SystemProfileConstants extends DConnectProfileConstants {

    /**
     * プロファイル名: {@value} .
     */
    String PROFILE_NAME = "system";

    /**
     * インターフェース: {@value} .
     */
    String INTERFACE_DEVICE = "device";
    
    /**
     * 属性: {@value} .
     */
    String ATTRIBUTE_EVENTS = "events";
    
    /**
     * 属性: {@value} .
     */
    String ATTRIBUTE_KEYWORD = "keyword";

    /**
     * 属性: {@value} .
     */
    String ATTRIBUTE_WAKEUP = "wakeup";

    /**
     * パス: {@value}.
     */
    String PATH_PROFILE = PATH_ROOT + SEPARATOR + PROFILE_NAME;

    /**
     * パス: {@value} .
     */
    String PATH_SYSTEM = PATH_PROFILE;

    /**
     * パス: {@value} .
     */
    String PATH_EVENTS = PATH_PROFILE + SEPARATOR + ATTRIBUTE_EVENTS;
    
    /**
     * パス: {@value} .
     */
    String PATH_KEYWORD = PATH_PROFILE + SEPARATOR + ATTRIBUTE_KEYWORD;

    /**
     * パス: {@value} .
     */
    String PATH_WAKEUP = PATH_PROFILE + SEPARATOR 
            + INTERFACE_DEVICE + SEPARATOR + "wakeup";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_SUPPORTS = "supports";

    /**
     * パラメータ: {@value}.
     */
    String PARAM_ID = "id";

    /**
     * パラメータ: {@value}.
     */
    String PARAM_NAME = "name";

    /**
     * パラメータ: {@value}.
     */
    String PARAM_PACKAGE_NAME = "packageName";

    /**
     * パラメータ: {@value}.
     */
    String PARAM_PLUGINS = "plugins";

    /**
     * パラメータ: {@value}.
     */
    String PARAM_PLUGIN_ID = "pluginId";

}
