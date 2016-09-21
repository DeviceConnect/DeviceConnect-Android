/*
 ECGProfileConstants.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.profile;

/**
 * ECG Profile API 定数群.<br/>
 * ECG Profile API のパラメータ名、インタフェース名、属性名、プロファイル名を定義する。
 *
 * @author NTT DOCOMO, INC.
 */
public interface ECGProfileConstants extends DConnectProfileConstants {
    /**
     * プロファイル名: {@value} .
     */
    String PROFILE_NAME = "ecg";
    /**
     * 属性: {@value} .
     */
    String ATTRIBUTE_ON_ECG = "onECG";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_ECG = "ecg";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_VALUE = "value";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_MDER_FLOAT = "mderFloat";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_TYPE = "type";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_TYPE_CODE = "typeCode";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_UNIT = "unit";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_UNIT_CODE = "unitCode";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_TIMESTAMP = "timeStamp";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_TIMESTAMP_STRING = "timeStampString";

}
