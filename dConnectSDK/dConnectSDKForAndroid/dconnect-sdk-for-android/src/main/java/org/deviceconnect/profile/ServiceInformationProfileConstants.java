/*
 ServiceInformationProfileConstants.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.profile;

/**
 * Service Information Profile API 定数群.<br>
 * Service Information Profile API のパラメータ名、インタフェース名、属性名、プロファイル名を定義する。
 * @author NTT DOCOMO, INC.
 */
public interface ServiceInformationProfileConstants extends DConnectProfileConstants {

    /**
     * プロファイル名: {@value} .
     */
    String PROFILE_NAME = "serviceInformation";

    /**
     * パス: {@value}.
     */
    String PATH_PROFILE = PATH_ROOT + SEPARATOR + PROFILE_NAME;

    /**
     * パラメータ: {@value} .
     */
    String PARAM_SUPPORTS = "supports";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_SUPPORT_APIS = "supportApis";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_CONNECT = "connect";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_WIFI = "wifi";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_BLUETOOTH = "bluetooth";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_NFC = "nfc";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_BLE = "ble";

    String PARAM_NAME = "name";

    String PARAM_VALUE = "value";

    String PARAM_FORMAT = "format";

    String PARAM_METHOD = "method";

    String PARAM_PATH = "path";

    String PARAM_REQUEST_PARAMS = "requestParams";

    String PARAM_TYPE = "type";

    String PARAM_MANDATORY = "mandatory";

    String PARAM_ENUM = "enum";

    String PARAM_MAX_LENGTH = "maxLength";

    String PARAM_MIN_LENGTH = "minLength";

    String PARAM_MAX_VALUE = "maxValue";

    String PARAM_MIN_VALUE = "minValue";

    String PARAM_EXCLUSIVE_MAX_VALUE = "exclusiveMaxValue";

    String PARAM_EXCLUSIVE_MIN_VALUE = "exclusiveMinValue";

    /**
     * 接続状態定数.
     */
    enum ConnectState {
        /**
         * 非対応.
         */
        NONE,
        /**
         * 接続ON.
         */
        ON,
        /**
         * 接続OFF.
         */
        OFF
    }

}
