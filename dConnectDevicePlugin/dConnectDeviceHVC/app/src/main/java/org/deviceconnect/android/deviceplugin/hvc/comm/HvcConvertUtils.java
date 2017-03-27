/*
 HvcConvertUtils.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.comm;

import android.util.Log;
import android.util.SparseArray;

import org.deviceconnect.android.deviceplugin.hvc.BuildConfig;
import org.deviceconnect.android.deviceplugin.hvc.HvcDeviceApplication;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectKind;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcConstants;
import org.deviceconnect.android.profile.HumanDetectionProfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import omron.HVC.HVC;


/**
 * HVC convert utility.
 * 
 * @author NTT DOCOMO, INC.
 *
 */
public final class HvcConvertUtils {

    /**
     * log tag.
     */
    private static final String TAG = HvcDeviceApplication.class.getSimpleName();
    
    /**
     * Constructor.
     */
    private HvcConvertUtils() {
        
    }
    
    
    /**
     * Convert threshold value.
     * @param normalizeThreshold threshold value(normalize value)
     * @return threshold value(HVC device value)
     */
    public static int convertToHvcThreshold(final double normalizeThreshold) {
        int hvcThreshold;
        hvcThreshold = (int) (normalizeThreshold * HvcConstants.THRESHOLD_MAX);
        return hvcThreshold;
    }

    /**
     * Convert width value.
     * @param normalizeWidth width value(normalize value)
     * @return width value(HVC device value)
     */
    public static int convertToHvcWidth(final double normalizeWidth) {
        return (int) (normalizeWidth * HvcConstants.HVC_C_CAMERA_WIDTH);
    }
    
    /**
     * Convert height value.
     * @param normalizeHeight height value(normalize value)
     * @return height value(HVC device value)
     */
    public static int convertToHvcHeight(final double normalizeHeight) {
        return (int) (normalizeHeight * HvcConstants.HVC_C_CAMERA_HEIGHT);
    }

    /**
     * Convert confidence value.
     * @param hvcConfidence confidence value(HVC device value)
     * @return confidence value(normalize value)
     */
    public static double convertToNormalizeConfidence(final int hvcConfidence) {
        return (double) hvcConfidence / HvcConstants.CONFIDENCE_MAX;
    }

    /**
     * Convert expression score value.
     * @param hvcExpressionScore expression score value(HVC device value)
     * @return expression score value(normalize value)
     */
    public static double convertToNormalizeExpressionScore(final double hvcExpressionScore) {
        return hvcExpressionScore / HvcConstants.EXPRESSION_SCORE_MAX;
    }
    
    
    
    /**
     * convert to normalize expression value.
     * @param hvcExpression expression(HVC value)
     * @return normalize expression value.
     */
    public static String convertToNormalizeExpression(final int hvcExpression) {
        
        String normalizeExpression = HvcConstants.EXPRESSION_UNKNOWN;
        
        SparseArray<String> map = new SparseArray<>();
        map.put(HVC.HVC_EX_NEUTRAL, HvcConstants.EXPRESSION_UNKNOWN);
        map.put(HVC.HVC_EX_HAPPINESS, HvcConstants.EXPRESSION_SMILE);
        map.put(HVC.HVC_EX_SURPRISE, HvcConstants.EXPRESSION_SURPRISE);
        map.put(HVC.HVC_EX_ANGER, HvcConstants.EXPRESSION_MAD);
        map.put(HVC.HVC_EX_SADNESS, HvcConstants.EXPRESSION_SAD);
        
        String exp = map.get(hvcExpression);
        if (exp != null) {
            normalizeExpression = exp;
        }
        
        return normalizeExpression;
    }

    /**
     * convert to normalize value by device value.
     * 
     * @param deviceValue device value
     * @param deviceMaxValue device value
     * @return normalizeValue
     */
    public static double convertToNormalize(final int deviceValue, final int deviceMaxValue) {
        return (double) deviceValue / (double) deviceMaxValue;
    }

    /**
     * convert to event attribute by detect kind.
     * @param detectKind detectKind
     * @return event attribute
     */
    public static String convertToEventAttribute(final HumanDetectKind detectKind) {
        
        Map<HumanDetectKind, String> map = new HashMap<>();
        map.put(HumanDetectKind.BODY, HumanDetectionProfile.ATTRIBUTE_ON_BODY_DETECTION);
        map.put(HumanDetectKind.HAND, HumanDetectionProfile.ATTRIBUTE_ON_HAND_DETECTION);
        map.put(HumanDetectKind.FACE, HumanDetectionProfile.ATTRIBUTE_ON_FACE_DETECTION);
        map.put(HumanDetectKind.HUMAN, "onDetection");
        
        String attribute = map.get(detectKind);
        if (attribute != null) {
            return attribute;
        }
        return null;
    }

    /**
     * convert useFunc.
     * 
     * @param detectKind detectKind
     * @param options options
     * @return useFunc
     */
    public static int convertUseFunc(final HumanDetectKind detectKind, final List<String> options) {

        HashMap<HumanDetectKind, Integer> convertDetectKinds = new HashMap<>();
        convertDetectKinds.put(HumanDetectKind.BODY, HVC.HVC_ACTIV_BODY_DETECTION);
        convertDetectKinds.put(HumanDetectKind.HAND, HVC.HVC_ACTIV_HAND_DETECTION);
        convertDetectKinds.put(HumanDetectKind.FACE, HVC.HVC_ACTIV_FACE_DETECTION);
        Integer detectBitFlag = convertDetectKinds.get(detectKind);
        if (detectBitFlag == null) {
            // not match
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "unknown detect kind. value: " + detectKind);
            }
            return 0;
        }
        
        HashMap<String, Integer> convertOptions = new HashMap<>();
        convertOptions.put(HumanDetectionProfile.VALUE_OPTION_FACE_DIRECTION, HVC.HVC_ACTIV_FACE_DIRECTION);
        convertOptions.put(HumanDetectionProfile.VALUE_OPTION_AGE, HVC.HVC_ACTIV_AGE_ESTIMATION);
        convertOptions.put(HumanDetectionProfile.VALUE_OPTION_GENDER, HVC.HVC_ACTIV_GENDER_ESTIMATION);
        convertOptions.put(HumanDetectionProfile.VALUE_OPTION_GAZE, HVC.HVC_ACTIV_GAZE_ESTIMATION);
        convertOptions.put(HumanDetectionProfile.VALUE_OPTION_BLINK, HVC.HVC_ACTIV_BLINK_ESTIMATION);
        convertOptions.put(HumanDetectionProfile.VALUE_OPTION_EXPRESSION, HVC.HVC_ACTIV_EXPRESSION_ESTIMATION);

        int optionBitFlag = 0;
        if (options != null) {
            for (String option : options) {
                Integer bitFlag = convertOptions.get(option);
                if (bitFlag != null) {
                    // match
                    optionBitFlag |= bitFlag;
                }
            }
        }

        return detectBitFlag | optionBitFlag;
    }
}
