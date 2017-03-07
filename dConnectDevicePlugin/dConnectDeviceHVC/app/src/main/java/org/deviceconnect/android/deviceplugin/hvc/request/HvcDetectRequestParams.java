/*
 HvcDetectRequestParams.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.request;

import org.deviceconnect.android.deviceplugin.hvc.comm.HvcConvertUtils;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectBodyRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectEventRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectFaceRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectHandRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectKind;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcConstants;

import java.util.ArrayList;

import omron.HVC.HVC_PRM;


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
     * HVC default event interval[msec].
     */
    private static final long DEFAULT_EVENT_INTERVAL = HvcConstants.PARAM_INTERVAL_DEFAULT;
    

    /**
     * get HVC default body request parameters.
     * @return HVC default body request parameters.
     */
    public static HumanDetectBodyRequestParams getDefaultBodyRequestParameter() {
        
        HumanDetectBodyRequestParams body = new HumanDetectBodyRequestParams(new ArrayList<String>(),
                DEFAULT_NORMALIZE_THRESHOLD, DEFAULT_NORMALIZE_BODY_MIN_WIDTH, DEFAULT_NORMALIZE_BODY_MIN_HEIGHT,
                DEFAULT_NORMALIZE_BODY_MAX_WIDTH, DEFAULT_NORMALIZE_BODY_MAX_HEIGHT);
        return body;
    }
    
    /**
     * get HVC default hand request parameters.
     * @return HVC default hand request parameters.
     */
    public static HumanDetectHandRequestParams getDefaultHandRequestParameter() {
        
        HumanDetectHandRequestParams hand = new HumanDetectHandRequestParams(new ArrayList<String>(),
                DEFAULT_NORMALIZE_THRESHOLD, DEFAULT_NORMALIZE_HAND_MIN_WIDTH, DEFAULT_NORMALIZE_HAND_MIN_HEIGHT,
                DEFAULT_NORMALIZE_HAND_MAX_WIDTH, DEFAULT_NORMALIZE_HAND_MAX_HEIGHT);
        return hand;
    }
    
    
    /**
     * get HVC default face request parameters.
     * @return HVC default face request parameters.
     */
    public static HumanDetectFaceRequestParams getDefaultFaceRequestParameter() {
        
        HumanDetectFaceRequestParams face = new HumanDetectFaceRequestParams(new ArrayList<String>(),
                DEFAULT_NORMALIZE_THRESHOLD, DEFAULT_NORMALIZE_FACE_MIN_WIDTH, DEFAULT_NORMALIZE_FACE_MIN_HEIGHT,
                DEFAULT_NORMALIZE_FACE_MAX_WIDTH, DEFAULT_NORMALIZE_FACE_MAX_HEIGHT);
        return face;
    }
    
    /**
     * get HVC default event request parameters.
     * @return HVC default event request parameters.
     */
    public static HumanDetectEventRequestParams getDefaultEventRequestParameter() {
        
        HumanDetectEventRequestParams event = new HumanDetectEventRequestParams(DEFAULT_EVENT_INTERVAL);
        return event;
    }

    /**
     * Human detect request parameters.
     */
    private HumanDetectRequestParams mRequestParams;
    
    /**
     * Constructor.
     * @param requestParams human detect request parameters.
     */
    public HvcDetectRequestParams(final HumanDetectRequestParams requestParams) {
        mRequestParams = requestParams;
    }
    
    /**
     * get HumanDetectRequestParams.
     * @return HumanDetectRequestParams
     */
    public HumanDetectRequestParams getHumanDetectRequestParams() {
        return mRequestParams;
    }
    
    
    /**
     * get HVC body request parameters.
     * @return HVC body request parameters.
     */
    public HvcBodyRequestParams getBody() {
        if (mRequestParams.getBody() != null) {
            HvcBodyRequestParams bodyRequestParams = new HvcBodyRequestParams(mRequestParams.getBody());
            return bodyRequestParams;
        } else {
            return null;
        }
    }
    
    /**
     * get HVC hand request parameters.
     * @return HVC hand request parameters.
     */
    public HvcHandRequestParams getHand() {
        if (mRequestParams.getHand() != null) {
            HvcHandRequestParams handRequestParams = new HvcHandRequestParams(mRequestParams.getHand());
            return handRequestParams;
        } else {
            return null;
        }
    }
    
    /**
     * get HVC face request parameters.
     * @return HVC face request parameters.
     */
    public HvcFaceRequestParams getFace() {
        if (mRequestParams.getFace() != null) {
            HvcFaceRequestParams faceRequestParams = new HvcFaceRequestParams(mRequestParams.getFace());
            return faceRequestParams;
        } else {
            return null;
        }
    }
    
    
    
    
    
    
    /**
     * get HVC_PRM value.
     * @return HVC_PRM value
     */
    public HVC_PRM getHvcParams() {
        HvcBodyRequestParams body = getBody();
        HvcHandRequestParams hand = getHand();
        HvcFaceRequestParams face = getFace();
        
        HVC_PRM hvcPrm = new HVC_PRM();
        
        if (body != null) {
            hvcPrm.body.Threshold = body.getHvcThreshold();
            hvcPrm.body.MinSize = body.getHvcMinWidth();
            hvcPrm.body.MaxSize = body.getHvcMaxWidth();
        }
        
        if (hand != null) {
            hvcPrm.hand.Threshold = hand.getHvcThreshold();
            hvcPrm.hand.MinSize = hand.getHvcMinWidth();
            hvcPrm.hand.MaxSize = hand.getHvcMaxWidth();
        }
        
        if (face != null) {
            hvcPrm.face.Threshold = face.getHvcThreshold();
            hvcPrm.face.MinSize = face.getHvcMinWidth();
            hvcPrm.face.MaxSize = face.getHvcMaxWidth();
        }
        
        return hvcPrm;
    }
    
    /**
     * get useFunc.
     * @return useFunc
     */
    public int getUseFunc() {
        int useFunc = 0;
        if (mRequestParams.getBody() != null) {
            useFunc |= HvcConvertUtils.convertUseFunc(HumanDetectKind.BODY, mRequestParams.getBody().getOptions());
        }
        if (mRequestParams.getHand() != null) {
            useFunc |= HvcConvertUtils.convertUseFunc(HumanDetectKind.HAND, mRequestParams.getHand().getOptions());
        }
        if (mRequestParams.getFace() != null) {
            useFunc |= HvcConvertUtils.convertUseFunc(HumanDetectKind.FACE, mRequestParams.getFace().getOptions());
        }
        return useFunc;
    }
}
