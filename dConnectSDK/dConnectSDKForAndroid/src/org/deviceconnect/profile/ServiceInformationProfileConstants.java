/*
 ServiceInformationProfileConstants.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.profile;

/**
 * Service Information Profile API 定数群.<br/>
 * Service Information Profile API のパラメータ名、インタフェース名、属性名、プロファイル名を定義する。
 * @author NTT DOCOMO, INC.
 */
public interface ServiceInformationProfileConstants extends DConnectProfileConstants {

    /**
     * プロファイル名: {@value} .
     */
    String PROFILE_NAME = "serviceinformation";

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
