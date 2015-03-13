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
public class HvcFaceRequestParams {

    /**
     * human detect face request parameters.
     */
    private HumanDetectFaceRequestParams mFaceRequestParams;
    
    
    /**
     * Constructor(with HumanDetectFaceRequestParams).
     * @param faceRequestParams face request parameters.
     */
    public HvcFaceRequestParams(final HumanDetectFaceRequestParams faceRequestParams) {
        mFaceRequestParams = faceRequestParams;
    }

    
    /**
     * get request parameters.
     * @return request parameters
     */
    public HumanDetectFaceRequestParams getRequestParams() {
        return mFaceRequestParams;
    }
    
    /**
     * Get threshold value(HVC device value).
     * @return HVC threshold(HVC device value)
     */
    public int getHvcThreshold() {
        int hvcThreshold = HvcConvertUtils.convertToHvcThreshold(mFaceRequestParams.getThreshold());
        return hvcThreshold;
    }
    
    /**
     * Get min width value(HVC device value).
     * @return HVC min width value(HVC device value)
     */
    public int getHvcMinWidth() {
        int hvcMinWidth = HvcConvertUtils.convertToHvcWidth(mFaceRequestParams.getMinWidth());
        return hvcMinWidth;
    }
    
    /**
     * Get min height value(HVC device value).
     * @return HVC min height value(HVC device value)
     */
    public int getHvcMinHeight() {
        int hvcMinHeight = HvcConvertUtils.convertToHvcHeight(mFaceRequestParams.getMinHeight());
        return hvcMinHeight;
    }
    
    /**
     * Get max width value(HVC device value).
     * @return HVC max width value(HVC device value)
     */
    public int getHvcMaxWidth() {
        int hvcMaxWidth = HvcConvertUtils.convertToHvcWidth(mFaceRequestParams.getMaxWidth());
        return hvcMaxWidth;
    }
    
    /**
     * Get max height value(HVC device value).
     * @return HVC max height value(HVC device value)
     */
    public int getHvcMaxHeight() {
        int hvcMaxHeight = HvcConvertUtils.convertToHvcHeight(mFaceRequestParams.getMaxHeight());
        return hvcMaxHeight;
    }
    
    /**
     * Get eye threshold value(HVC device value).
     * @return HVC eye threshold(HVC device value)
     */
    public int getHvcEyeThreshold() {
        int hvcEyeThreshold = HvcConvertUtils.convertToHvcThreshold(mFaceRequestParams.getEyeThreshold());
        return hvcEyeThreshold;
    }
    
    /**
     * Get nose threshold value(HVC device value).
     * @return HVC nose threshold(HVC device value)
     */
    public int getHvcNoseThreshold() {
        int hvcNoseThreshold = HvcConvertUtils.convertToHvcThreshold(mFaceRequestParams.getNoseThreshold());
        return hvcNoseThreshold;
    }
    
    /**
     * Get mouth threshold value(HVC device value).
     * @return HVC mouth threshold(HVC device value)
     */
    public int getHvcMouthThreshold() {
        int hvcMouthThreshold = HvcConvertUtils.convertToHvcThreshold(mFaceRequestParams.getMouthThreshold());
        return hvcMouthThreshold;
    }
    
    /**
     * Get blink threshold value(HVC device value).
     * @return HVC blink threshold(HVC device value)
     */
    public int getHvcBlinkThreshold() {
        int hvcBlinkThreshold = HvcConvertUtils.convertToHvcThreshold(mFaceRequestParams.getBlinkThreshold());
        return hvcBlinkThreshold;
    }
    
    /**
     * Get age threshold value(HVC device value).
     * @return HVC age threshold(HVC device value)
     */
    public int getHvcAgeThreshold() {
        int hvcAgeThreshold = HvcConvertUtils.convertToHvcThreshold(mFaceRequestParams.getAgeThreshold());
        return hvcAgeThreshold;
    }
    
    /**
     * Get gender threshold value(HVC device value).
     * @return HVC gender threshold(HVC device value)
     */
    public int getHvcGenderThreshold() {
        int hvcGenderThreshold = HvcConvertUtils.convertToHvcThreshold(mFaceRequestParams.getGenderThreshold());
        return hvcGenderThreshold;
    }
    
    /**
     * Get face direction threshold value(HVC device value).
     * @return HVC face direction threshold(HVC device value)
     */
    public int getHvcFaceDirectionThreshold() {
        int hvcFaceDirectionThreshold = HvcConvertUtils.convertToHvcThreshold(mFaceRequestParams
                .getFaceDirectionThreshold());
        return hvcFaceDirectionThreshold;
    }
    
    /**
     * Get gaze threshold value(HVC device value).
     * @return HVC gaze threshold(HVC device value)
     */
    public int getHvcGazeThreshold() {
        int hvcGazeThreshold = HvcConvertUtils.convertToHvcThreshold(mFaceRequestParams.getGazeThreshold());
        return hvcGazeThreshold;
    }
    
    /**
     * Get expression threshold value(HVC device value).
     * @return HVC expression threshold(HVC device value)
     */
    public int getHvcExpressionThreshold() {
        int hvcExpressionThreshold = HvcConvertUtils.convertToHvcThreshold(mFaceRequestParams.getExpressionThreshold());
        return hvcExpressionThreshold;
    }

}
