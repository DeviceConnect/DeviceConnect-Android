/*
 HvcFaceRequestParams.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.request;

import org.deviceconnect.android.deviceplugin.hvc.comm.HvcConvertUtils;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectFaceRequestParams;

/**
 * HVC face detect request parameter class.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcFaceRequestParams extends HumanDetectFaceRequestParams {

    /**
     * Constructor(with default value).
     * @param normalizeThreshold threshold
     * @param normalizeMinWidth minWidth
     * @param normalizeMinHeight minHeight
     * @param normalizeMaxWidth maxWidth
     * @param normalizeMaxHeight maxHeight
     */
    public HvcFaceRequestParams(final double normalizeThreshold, final double normalizeMinWidth,
            final double normalizeMinHeight, final double normalizeMaxWidth, final double normalizeMaxHeight) {
        super(normalizeThreshold, normalizeMinWidth, normalizeMinHeight, normalizeMaxWidth, normalizeMaxHeight);
        // TODO Auto-generated constructor stub
    }

    
    /**
     * Get threshold value(HVC device value).
     * @return HVC threshold(HVC device value)
     */
    public int getHvcThreshold() {
        int hvcThreshold = HvcConvertUtils.convertToHvcThreshold(getThreshold());
        return hvcThreshold;
    }
    
    /**
     * Get min width value(HVC device value).
     * @return HVC min width value(HVC device value)
     */
    public int getHvcMinWidth() {
        int hvcMinWidth = HvcConvertUtils.convertToHvcWidth(getMinWidth());
        return hvcMinWidth;
    }
    
    /**
     * Get min height value(HVC device value).
     * @return HVC min height value(HVC device value)
     */
    public int getHvcMinHeight() {
        int hvcMinHeight = HvcConvertUtils.convertToHvcHeight(getMinHeight());
        return hvcMinHeight;
    }
    
    /**
     * Get max width value(HVC device value).
     * @return HVC max width value(HVC device value)
     */
    public int getHvcMaxWidth() {
        int hvcMaxWidth = HvcConvertUtils.convertToHvcWidth(getMaxWidth());
        return hvcMaxWidth;
    }
    
    /**
     * Get max height value(HVC device value).
     * @return HVC max height value(HVC device value)
     */
    public int getHvcMaxHeight() {
        int hvcMaxHeight = HvcConvertUtils.convertToHvcHeight(getMaxHeight());
        return hvcMaxHeight;
    }
    
    /**
     * Get eye threshold value(HVC device value).
     * @return HVC eye threshold(HVC device value)
     */
    public int getHvcEyeThreshold() {
        int hvcEyeThreshold = HvcConvertUtils.convertToHvcThreshold(getEyeThreshold());
        return hvcEyeThreshold;
    }
    
    /**
     * Get nose threshold value(HVC device value).
     * @return HVC nose threshold(HVC device value)
     */
    public int getHvcNoseThreshold() {
        int hvcNoseThreshold = HvcConvertUtils.convertToHvcThreshold(getNoseThreshold());
        return hvcNoseThreshold;
    }
    
    /**
     * Get mouth threshold value(HVC device value).
     * @return HVC mouth threshold(HVC device value)
     */
    public int getHvcMouthThreshold() {
        int hvcMouthThreshold = HvcConvertUtils.convertToHvcThreshold(getMouthThreshold());
        return hvcMouthThreshold;
    }
    
    /**
     * Get blink threshold value(HVC device value).
     * @return HVC blink threshold(HVC device value)
     */
    public int getHvcBlinkThreshold() {
        int hvcBlinkThreshold = HvcConvertUtils.convertToHvcThreshold(getBlinkThreshold());
        return hvcBlinkThreshold;
    }
    
    /**
     * Get age threshold value(HVC device value).
     * @return HVC age threshold(HVC device value)
     */
    public int getHvcAgeThreshold() {
        int hvcAgeThreshold = HvcConvertUtils.convertToHvcThreshold(getAgeThreshold());
        return hvcAgeThreshold;
    }
    
    /**
     * Get gender threshold value(HVC device value).
     * @return HVC gender threshold(HVC device value)
     */
    public int getHvcGenderThreshold() {
        int hvcGenderThreshold = HvcConvertUtils.convertToHvcThreshold(getGenderThreshold());
        return hvcGenderThreshold;
    }
    
    /**
     * Get face direction threshold value(HVC device value).
     * @return HVC face direction threshold(HVC device value)
     */
    public int getHvcFaceDirectionThreshold() {
        int hvcFaceDirectionThreshold = HvcConvertUtils.convertToHvcThreshold(getFaceDirectionThreshold());
        return hvcFaceDirectionThreshold;
    }
    
    /**
     * Get gaze threshold value(HVC device value).
     * @return HVC gaze threshold(HVC device value)
     */
    public int getHvcGazeThreshold() {
        int hvcGazeThreshold = HvcConvertUtils.convertToHvcThreshold(getGazeThreshold());
        return hvcGazeThreshold;
    }
    
    /**
     * Get expression threshold value(HVC device value).
     * @return HVC expression threshold(HVC device value)
     */
    public int getHvcExpressionThreshold() {
        int hvcExpressionThreshold = HvcConvertUtils.convertToHvcThreshold(getExpressionThreshold());
        return hvcExpressionThreshold;
    }
}
