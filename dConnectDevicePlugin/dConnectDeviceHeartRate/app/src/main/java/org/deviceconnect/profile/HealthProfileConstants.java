/*
 HealthProfileConstants.java
 Copyright (c) 2014 NTT DOCOMO,INC.
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
     * パラメータ: {@value} .
     */
    String PARAM_HEART_RATE = "heartRate";
}
