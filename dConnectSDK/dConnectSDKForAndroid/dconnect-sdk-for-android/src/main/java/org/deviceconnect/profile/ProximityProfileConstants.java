/*
 ProximityProfileConstants.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.profile;

/**
 * Proximity Profile API 定数群.<br>
 * Proximity Profile API のパラメータ名、インタフェース名、属性名、プロファイル名を定義する。
 * @deprecated swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 * @author NTT DOCOMO, INC.
 */
public interface ProximityProfileConstants extends DConnectProfileConstants {

    /**
     * プロファイル名: {@value} .
     */
    String PROFILE_NAME = "proximity";

    /**
     * 属性: {@value} .
     */
    String ATTRIBUTE_ON_DEVICE_PROXIMITY = "onDeviceProximity";

    /**
     * 属性: {@value} .
     */
    String ATTRIBUTE_ON_USER_PROXIMITY = "onUserProximity";

    /**
     * パス: {@value}.
     */
    String PATH_PROFILE = PATH_ROOT + SEPARATOR + PROFILE_NAME;

    /**
     * パス: {@value} .
     */
    String PATH_ON_DEVICE_PROXIMITY = PATH_PROFILE + SEPARATOR + ATTRIBUTE_ON_DEVICE_PROXIMITY;

    /**
     * パス: {@value} .
     */
    String PATH_ON_USER_PROXIMITY = PATH_PROFILE + SEPARATOR + ATTRIBUTE_ON_USER_PROXIMITY;


    /**
     * パラメータ: {@value} .
     */
    String PARAM_VALUE = "value";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_MIN = "min";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_MAX = "max";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_THRESHOLD = "threshold";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_PROXIMITY = "proximity";

    /**
     * パラメータ: {@value}.
     */
    String PARAM_NEAR = "near";

    /**
     * パラメータ: {@value}.
     */
    String PARAM_RANGE = "range";

    enum Range {
        IMMEDIATE("IMMEDIATE"),
        NEAR("NEAR"),
        FAR("FAR"),
        UNKNOWN("UNKNOWN");

        private String mValue;

        Range(String value) {
            mValue = value;
        }

        public String getValue() {
            return mValue;
        }
    }
}
