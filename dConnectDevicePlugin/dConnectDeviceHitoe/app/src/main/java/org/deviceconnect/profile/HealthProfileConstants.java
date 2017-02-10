/*
 HealthProfileConstants.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.profile;

/**
 * Health Profile API 定数群.<br/>
 * Health Profile API のパラメータ名、インタフェース名、属性名、プロファイル名を定義する。
 * 
 * @author NTT DOCOMO, INC.
 */
public interface HealthProfileConstants extends DConnectProfileConstants {
    /**
     * プロファイル名: {@value} .
     */
    String PROFILE_NAME = "health";
    
    /**
     * 属性: {@value} .
     */
    String ATTRIBUTE_HEART_RATE = "heartrate";

    /**
     * 属性: {@value} .
     */
    String ATTRIBUTE_HEART = "heart";
    /**
     * 属性: {@value} .
     */
    String ATTRIBUTE_ONHEART = "onHeart";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_HEART_RATE = "heartRate";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_HEART = "heart";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_RATE = "rate";
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

    /**
     * パラメータ: {@value} .
     */
    String PARAM_RR = "rr";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_ENERGY = "energy";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_DEVICE = "device";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_PRODUCT_NAME = "productName";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_MANUFACTURER_NAME = "manufacturerName";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_MODEL_NUMBER = "modelNumber";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_FIRMWARE_REVISION = "firmwareRevision";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_SERIAL_NUMBER = "serialNumber";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_SOFTWARE_REVISION = "softwareRevision";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_HARDWARE_REVISION = "hardwareRevision";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_PART_NUMBER = "partNumber";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_PROTOCOL_REVISION = "protocolRevision";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_SYSTEM_ID = "systemId";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_BATTERY_LEVEL = "batteryLevel";

    /**
     * パス: {@value}.
     */
    String PATH_HEARTRATE = PROFILE_NAME + SEPARATOR + ATTRIBUTE_HEART_RATE;

    /**
     * パス: {@value}.
     */
    String PATH_HEART = PROFILE_NAME + SEPARATOR + ATTRIBUTE_HEART;

}
