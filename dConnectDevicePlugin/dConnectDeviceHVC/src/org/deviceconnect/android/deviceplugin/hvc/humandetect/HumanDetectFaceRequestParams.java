/*
 HumanDetectFaceRequestParams.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.humandetect;

import java.util.List;

/**
 * face detect request parameter.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HumanDetectFaceRequestParams extends HumanDetectBasicRequestParams implements Cloneable {
    
    /**
     * eye threshold.
     */
    private double mEyeThreshold;
    /**
     * nose threshold.
     */
    private double mNoseThreshold;
    /**
     * mouth threshold.
     */
    private double mMouthThreshold;
    /**
     * blink threshold.
     */
    private double mBlinkThreshold;
    /**
     * age threshold.
     */
    private double mAgeThreshold;
    /**
     * gender threshold.
     */
    private double mGenderThreshold;
    /**
     * face direction threshold.
     */
    private double mFaceDirectionThreshold;
    /**
     * gaze threshold.
     */
    private double mGazeThreshold;
    /**
     * expression threshold.
     */
    private double mExpressionThreshold;
    
    
    /**
     * Constructor(with default value).
     * @param options options
     * @param normalizeThreshold threshold
     * @param normalizeMinWidth minWidth
     * @param normalizeMinHeight minHeight
     * @param normalizeMaxWidth maxWidth
     * @param normalizeMaxHeight maxHeight
     */
    public HumanDetectFaceRequestParams(final List<String> options, final double normalizeThreshold,
            final double normalizeMinWidth, final double normalizeMinHeight, final double normalizeMaxWidth,
            final double normalizeMaxHeight) {
        super(options, normalizeThreshold, normalizeMinWidth, normalizeMinHeight, normalizeMaxWidth, 
                normalizeMaxHeight);
        
        mEyeThreshold = normalizeThreshold;
        mNoseThreshold = normalizeThreshold;
        mMouthThreshold = normalizeThreshold;
        mBlinkThreshold = normalizeThreshold;
        mAgeThreshold = normalizeThreshold;
        mGenderThreshold = normalizeThreshold;
        mFaceDirectionThreshold = normalizeThreshold;
        mGazeThreshold = normalizeThreshold;
        mExpressionThreshold = normalizeThreshold;
    }

    /**
     * get eye threshold.
     * @return eye threshold
     */
    public double getEyeThreshold() {
        return mEyeThreshold;
    }
    
    /**
     * get nose threshold.
     * @return nose threshold
     */
    public double getNoseThreshold() {
        return mNoseThreshold;
    }
    
    /**
     * get mouth threshold.
     * @return mouth threshold
     */
    public double getMouthThreshold() {
        return mMouthThreshold;
    }
    
    /**
     * get blink threshold.
     * @return blink threshold
     */
    public double getBlinkThreshold() {
        return mBlinkThreshold;
    }
    
    /**
     * get age threshold.
     * @return age threshold
     */
    public double getAgeThreshold() {
        return mAgeThreshold;
    }
    
    /**
     * gender threshold.
     * @return gender threshold
     */
    public double getGenderThreshold() {
        return mGenderThreshold;
    }
    
    /**
     * face direction threshold.
     * @return face direction threshold
     */
    public double getFaceDirectionThreshold() {
        return mFaceDirectionThreshold;
    }
    
    /**
     * gaze threshold.
     * @return gaze threshold
     */
    public double getGazeThreshold() {
        return mGazeThreshold;
    }
    
    /**
     * get expression threshold.
     * @return expression threshold
     */
    public double getExpressionThreshold() {
        return mExpressionThreshold;
    }



    /**
     * set eye threshold.
     * @param eyeThreshold eye threshold
     */
    public void setEyeThreshold(final double eyeThreshold) {
        mEyeThreshold = eyeThreshold;
    }
    
    /**
     * set nose threshold.
     * @param noseThreshold nose threshold
     */
    public void setNoseThreshold(final double noseThreshold) {
        mNoseThreshold = noseThreshold;
    }
    
    /**
     * set mouth threshold.
     * @param mouthThreshold mouth threshold
     */
    public void setMouthThreshold(final double mouthThreshold) {
        mMouthThreshold = mouthThreshold;
    }
    
    /**
     * set blink threshold.
     * @param blinkThreshold blink threshold
     */
    public void setBlinkThreshold(final double blinkThreshold) {
        mBlinkThreshold = blinkThreshold;
    }
    
    /**
     * set age threshold.
     * @param ageThreshold age threshold
     */
    public void setAgeThreshold(final double ageThreshold) {
        mAgeThreshold = ageThreshold;
    }
    
    /**
     * set gender threshold.
     * @param genderThreshold gender threshold
     */
    public void setGenderThreshold(final double genderThreshold) {
        mGenderThreshold = genderThreshold;
    }
    
    /**
     * set face direction threshold.
     * @param faceDirectionThreshold face direction threshold
     */
    public void setFaceDirectionThreshold(final double faceDirectionThreshold) {
        mFaceDirectionThreshold = faceDirectionThreshold;
    }
    
    /**
     * set gaze threshold.
     * @param gazeThreshold gaze threshold
     */
    public void setGazeThreshold(final double gazeThreshold) {
        mGazeThreshold = gazeThreshold;
    }
    
    /**
     * set expression threshold.
     * @param expressionThreshold expression threshold
     */
    public void setExpressionThreshold(final double expressionThreshold) {
        mExpressionThreshold = expressionThreshold;
    }
}
