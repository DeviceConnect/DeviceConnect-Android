/*
 HvcDetectRequestParams.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.request;

import omron.HVC.HVC_PRM;

import org.deviceconnect.android.deviceplugin.hvc.profile.HvcConstants;

/**
 * HVC request parameter class.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcDetectRequestParams {

    /**
     * HVC default threshold value.
     */
    private static final double DEFAULT_NORMALIZE_THRESHOLD = (double) (HvcConstants.THRESHOLD_DEFAULT)
            / HvcConstants.THRESHOLD_MAX;

    /**
     * HVC default body min width value.
     */
    private static final double DEFAULT_NORMALIZE_BODY_MIN_WIDTH = (double) (HvcConstants.BODY_MIN_WIDTH_DEFAULT)
            / HvcConstants.HVC_C_CAMERA_WIDTH;

    /**
     * HVC default body min height value.
     */
    private static final double DEFAULT_NORMALIZE_BODY_MIN_HEIGHT = (double) (HvcConstants.BODY_MIN_HEIGHT_DEFAULT)
            / HvcConstants.HVC_C_CAMERA_HEIGHT;

    /**
     * HVC default body max width value.
     */
    private static final double DEFAULT_NORMALIZE_BODY_MAX_WIDTH = (double) (HvcConstants.BODY_MAX_WIDTH_DEFAULT)
            / HvcConstants.HVC_C_CAMERA_WIDTH;

    /**
     * HVC default body max height value.
     */
    private static final double DEFAULT_NORMALIZE_BODY_MAX_HEIGHT = (double) (HvcConstants.BODY_MAX_HEIGHT_DEFAULT)
            / HvcConstants.HVC_C_CAMERA_HEIGHT;

    /**
     * HVC default hand min width value.
     */
    private static final double DEFAULT_NORMALIZE_HAND_MIN_WIDTH = (double) (HvcConstants.HAND_MIN_WIDTH_DEFAULT)
            / HvcConstants.HVC_C_CAMERA_WIDTH;

    /**
     * HVC default hand min height value.
     */
    private static final double DEFAULT_NORMALIZE_HAND_MIN_HEIGHT = (double) (HvcConstants.HAND_MIN_HEIGHT_DEFAULT)
            / HvcConstants.HVC_C_CAMERA_HEIGHT;

    /**
     * HVC default hand max width value.
     */
    private static final double DEFAULT_NORMALIZE_HAND_MAX_WIDTH = (double) (HvcConstants.HAND_MAX_WIDTH_DEFAULT)
            / HvcConstants.HVC_C_CAMERA_WIDTH;

    /**
     * HVC default hand max height value.
     */
    private static final double DEFAULT_NORMALIZE_HAND_MAX_HEIGHT = (double) (HvcConstants.HAND_MAX_HEIGHT_DEFAULT)
            / HvcConstants.HVC_C_CAMERA_HEIGHT;

    /**
     * HVC default face min width value.
     */
    private static final double DEFAULT_NORMALIZE_FACE_MIN_WIDTH = (double) (HvcConstants.FACE_MIN_WIDTH_DEFAULT)
            / HvcConstants.HVC_C_CAMERA_WIDTH;

    /**
     * HVC default face max height value.
     */
    private static final double DEFAULT_NORMALIZE_FACE_MIN_HEIGHT = (double) (HvcConstants.FACE_MIN_HEIGHT_DEFAULT)
            / HvcConstants.HVC_C_CAMERA_HEIGHT;

    /**
     * HVC default face min width value.
     */
    private static final double DEFAULT_NORMALIZE_FACE_MAX_WIDTH = (double) (HvcConstants.FACE_MAX_WIDTH_DEFAULT)
            / HvcConstants.HVC_C_CAMERA_WIDTH;

    /**
     * HVC default face max height value.
     */
    private static final double DEFAULT_NORMALIZE_FACE_MAX_HEIGHT = (double) (HvcConstants.FACE_MAX_HEIGHT_DEFAULT)
            / HvcConstants.HVC_C_CAMERA_HEIGHT;

    /**
     * body parameter.
     */
    private HvcBodyRequestParams mBody;
    /**
     * hand parameter.
     */
    private HvcHandRequestParams mHand;
    /**
     * face parameter.
     */
    private HvcFaceRequestParams mFace;

    /**
     * Constructor.
     */
    public HvcDetectRequestParams() {
        /* set default value. */
        mBody = new HvcBodyRequestParams(DEFAULT_NORMALIZE_THRESHOLD, DEFAULT_NORMALIZE_BODY_MIN_WIDTH,
                DEFAULT_NORMALIZE_BODY_MIN_HEIGHT, DEFAULT_NORMALIZE_BODY_MAX_WIDTH, DEFAULT_NORMALIZE_BODY_MAX_HEIGHT);
        mHand = new HvcHandRequestParams(DEFAULT_NORMALIZE_THRESHOLD, DEFAULT_NORMALIZE_HAND_MIN_WIDTH,
                DEFAULT_NORMALIZE_HAND_MIN_HEIGHT, DEFAULT_NORMALIZE_HAND_MAX_WIDTH, DEFAULT_NORMALIZE_HAND_MAX_HEIGHT);
        mFace = new HvcFaceRequestParams(DEFAULT_NORMALIZE_THRESHOLD, DEFAULT_NORMALIZE_FACE_MIN_WIDTH,
                DEFAULT_NORMALIZE_FACE_MIN_HEIGHT, DEFAULT_NORMALIZE_FACE_MAX_WIDTH, DEFAULT_NORMALIZE_FACE_MAX_HEIGHT);
    }

    /**
     * get body parameter.
     * 
     * @return body parameter
     */
    public HvcBodyRequestParams getBody() {
        return mBody;
    }

    /**
     * get hand parameter.
     * 
     * @return hand parameter
     */
    public HvcHandRequestParams getHand() {
        return mHand;
    }

    /**
     * get face parameter.
     * 
     * @return face parameter
     */
    public HvcFaceRequestParams getFace() {
        return mFace;
    }

    //
    // setBodyNormalize***()
    //

    /**
     * set body threshold(normalize value).
     * @param bodyThreshold body threshold(normalize value)
     */
    public void setBodyNormalizeThreshold(final double bodyThreshold) {
        mBody.setThreshold(bodyThreshold);
    }

    /**
     * set body min width(normalize value).
     * @param bodyMinWidth body min width(normalize value)
     */
    public void setBodyNormalizeMinWidth(final double bodyMinWidth) {
        mBody.setMinWidth(bodyMinWidth);
    }

    /**
     * set body min height(normalize value).
     * @param bodyMinHeight body min height(normalize value)
     */
    public void setBodyNormalizeMinHeight(final double bodyMinHeight) {
        mBody.setMinHeight(bodyMinHeight);
    }

    /**
     * set body max width(normalize value).
     * @param bodyMaxWidth body max width(normalize value)
     */
    public void setBodyNormalizeMaxWidth(final double bodyMaxWidth) {
        mBody.setMaxWidth(bodyMaxWidth);
    }

    /**
     * set body max height(normalize value).
     * @param bodyMaxHeight body max height(normalize value)
     */
    public void setBodyNormalizeMaxHeight(final double bodyMaxHeight) {
        mBody.setMaxHeight(bodyMaxHeight);
    }

    //
    // setHandNormalize***()
    //

    /**
     * set hand threshold(normalize value).
     * @param handThreshold hand threshold(normalize value)
     */
    public void setHandNormalizeThreshold(final double handThreshold) {
        mHand.setThreshold(handThreshold);
    }

    /**
     * set hand min width(normalize value).
     * @param handMinWidth hand min width(normalize value)
     */
    public void setHandNormalizeMinWidth(final double handMinWidth) {
        mHand.setMinWidth(handMinWidth);
    }

    /**
     * set hand min height(normalize value).
     * @param handMinHeight hand min height(normalize value)
     */
    public void setHandNormalizeMinHeight(final double handMinHeight) {
        mHand.setMinHeight(handMinHeight);
    }

    /**
     * set hand max width(normalize value).
     * @param handMaxWidth hand max width(normalize value)
     */
    public void setHandNormalizeMaxWidth(final double handMaxWidth) {
        mHand.setMaxWidth(handMaxWidth);
    }

    /**
     * set hand max height(normalize value).
     * @param handMaxHeight hand max height(normalize value)
     */
    public void setHandNormalizeMaxHeight(final double handMaxHeight) {
        mHand.setMaxHeight(handMaxHeight);
    }
    
    //
    // setFaceNormalize***()
    //


    /**
     * set face threshold(normalize value).
     * @param faceThreshold face threshold(normalize value)
     */
    public void setFaceNormalizeThreshold(final double faceThreshold) {
        mFace.setThreshold(faceThreshold);
    }

    /**
     * set face min width(normalize value).
     * @param faceMinWidth face min width(normalize value)
     */
    public void setFaceNormalizeMinWidth(final double faceMinWidth) {
        mFace.setMinWidth(faceMinWidth);
    }

    /**
     * set face min height(normalize value).
     * @param faceMinHeight face min height(normalize value)
     */
    public void setFaceNormalizeMinHeight(final double faceMinHeight) {
        mFace.setMinHeight(faceMinHeight);
    }

    /**
     * set face max width(normalize value).
     * @param faceMaxWidth face max width(normalize value)
     */
    public void setFaceNormalizeMaxWidth(final double faceMaxWidth) {
        mFace.setMaxWidth(faceMaxWidth);
    }

    /**
     * set face max height(normalize value).
     * @param faceMaxHeight face max height(normalize value)
     */
    public void setFaceNormalizeMaxHeight(final double faceMaxHeight) {
        mFace.setMaxHeight(faceMaxHeight);
    }
    
    
    /**
     * set face eye threshold(normalize value).
     * @param eyeThreshold eye threshold(normalize value)
     */
    public void setFaceEyeNormalizeThreshold(final double eyeThreshold) {
        mFace.setEyeThreshold(eyeThreshold);
    }
    
    /**
     * set face nose threshold(normalize value).
     * @param noseThreshold nose threshold(normalize value)
     */
    public void setFaceNoseNormalizeThreshold(final double noseThreshold) {
        mFace.setNoseThreshold(noseThreshold);
    }
    
    /**
     * set face mouth threshold(normalize value).
     * @param mouthThreshold mouth threshold(normalize value)
     */
    public void setFaceMouthNormalizeThreshold(final double mouthThreshold) {
        mFace.setMouthThreshold(mouthThreshold);
    }

    /**
     * set face blink threshold(normalize value).
     * @param blinkThreshold blink threshold(normalize value)
     */
    public void setFaceBlinkNormalizeThreshold(final double blinkThreshold) {
        mFace.setMouthThreshold(blinkThreshold);
    }

    /**
     * set face age threshold(normalize value).
     * @param ageThreshold age threshold(normalize value)
     */
    public void setFaceAgeNormalizeThreshold(final double ageThreshold) {
        mFace.setAgeThreshold(ageThreshold);
    }

    /**
     * set face gender threshold(normalize value).
     * @param genderThreshold gender threshold(normalize value)
     */
    public void setFaceGenderNormalizeThreshold(final double genderThreshold) {
        mFace.setGenderThreshold(genderThreshold);
    }

    /**
     * set face direction threshold(normalize value).
     * @param faceDirectionThreshold face direction threshold(normalize value)
     */
    public void setFaceDirectionNormalizeThreshold(final double faceDirectionThreshold) {
        mFace.setFaceDirectionThreshold(faceDirectionThreshold);
    }

    /**
     * set face gaze threshold(normalize value).
     * @param gazeThreshold gaze threshold(normalize value)
     */
    public void setFaceGazeNormalizeThreshold(final double gazeThreshold) {
        mFace.setGazeThreshold(gazeThreshold);
    }

    /**
     * set face expression threshold(normalize value).
     * @param expressionThreshold expression threshold(normalize value)
     */
    public void setFaceExpressionNormalizeThreshold(final double expressionThreshold) {
        mFace.setExpressionThreshold(expressionThreshold);
    }
    
    
//    /**
//     * set body threshold(hvc device value).
//     * @param hvcBodyThreshold body threshold(hvc device value)
//     */
//    public void setBodyHvcThreshold(final int hvcBodyThreshold) {
//        double bodyThreshold = HvcConvertUtils.convertToNormalizeThreshold(hvcBodyThreshold);
//        mBody.setThreshold(bodyThreshold);
//    }
//
//    /**
//     * set body min width(hvc device value).
//     * @param hvcBodyMinWidth body min width(hvc device value)
//     */
//    public void setBodyHvcMinWidth(final int hvcBodyMinWidth) {
//        double bodyMinWidth = HvcConvertUtils.convertToNormalizeWidth(hvcBodyMinWidth);
//        mBody.setMinWidth(bodyMinWidth);
//    }
//
//    /**
//     * set body min height(hvc device value).
//     * @param hvcBodyMinHeight body min height(hvc device value)
//     */
//    public void setBodyHvcMinHeight(final int hvcBodyMinHeight) {
//        double bodyMinHeight = HvcConvertUtils.convertToNormalizeHeight(hvcBodyMinHeight);
//        mBody.setMinHeight(bodyMinHeight);
//    }
//
//    /**
//     * set body max width(hvc device value).
//     * @param hvcBodyMaxWidth body max width(hvc device value)
//     */
//    public void setBodyHvcMaxWidth(final int hvcBodyMaxWidth) {
//        double bodyMaxWidth = HvcConvertUtils.convertToNormalizeWidth(hvcBodyMaxWidth);
//        mBody.setMaxWidth(bodyMaxWidth);
//    }
//
//    /**
//     * set body max height(hvc device value).
//     * @param hvcBodyMaxHeight body max height(hvc device value)
//     */
//    public void setBodyHvcMaxHeight(final int hvcBodyMaxHeight) {
//        double bodyMaxHeight = HvcConvertUtils.convertToNormalizeHeight(hvcBodyMaxHeight);
//        mBody.setMaxHeight(bodyMaxHeight);
//    }
    
    
    
    /**
     * get HVC_PRM value.
     * @return HVC_PRM value
     */
    public HVC_PRM getHvcParams() {
        HVC_PRM hvcPrm = new HVC_PRM();
        
        hvcPrm.body.Threshold = mBody.getHvcThreshold();
        hvcPrm.body.MinSize = mBody.getHvcMinWidth();
        hvcPrm.body.MaxSize = mBody.getHvcMaxWidth();
        
        hvcPrm.hand.Threshold = mHand.getHvcThreshold();
        hvcPrm.hand.MinSize = mHand.getHvcMinWidth();
        hvcPrm.hand.MaxSize = mHand.getHvcMaxWidth();
        
        hvcPrm.face.Threshold = mHand.getHvcThreshold();
        hvcPrm.face.MinSize = mHand.getHvcMinWidth();
        hvcPrm.face.MaxSize = mHand.getHvcMaxWidth();
        
        return hvcPrm;
    }
    
}
