/*
 StressEstimationProfileConstants.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.profile;

/**
 * StressEstimation Profile API 定数群.<br/>
 * StressEstimation Profile API のパラメータ名、インタフェース名、属性名、プロファイル名を定義する。
 *
 * @author NTT DOCOMO, INC.
 */
public interface StressEstimationProfileConstants extends DConnectProfileConstants {
    /**
     * プロファイル名: {@value} .
     */
    String PROFILE_NAME = "stressEstimation";
    /**
     * 属性: {@value} .
     */
    String ATTRIBUTE_ON_STRESS_ESTIMATION = "onStressEstimation";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_STRESS = "stress";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_LFHF = "lfhf";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_TIMESTAMP = "timeStamp";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_TIMESTAMP_STRING = "timeStampString";

}
