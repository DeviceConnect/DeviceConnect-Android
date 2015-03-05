/*
 HvcDetectRequestParams.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.request;

import java.util.ArrayList;

import omron.HVC.HVC_PRM;

import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectBodyRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectFaceRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectHandRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectRequestParams;
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
     * HVC default event interval[msec].
     */
    private static final long DEFAULT_EVENT_INTERVAL = 10 * 1000;
    

    /**
     * get HVC default request parameters.
     * @return HVC default request parameters.
     */
    public static HumanDetectRequestParams getDefaultRequestParameter() {
        
        HumanDetectBodyRequestParams body = new HumanDetectBodyRequestParams(new ArrayList<String>(),
                DEFAULT_NORMALIZE_THRESHOLD, DEFAULT_NORMALIZE_BODY_MIN_WIDTH, DEFAULT_NORMALIZE_BODY_MIN_HEIGHT,
                DEFAULT_NORMALIZE_BODY_MAX_WIDTH, DEFAULT_NORMALIZE_BODY_MAX_HEIGHT, DEFAULT_EVENT_INTERVAL);
        
        HumanDetectHandRequestParams hand = new HumanDetectHandRequestParams(new ArrayList<String>(),
                DEFAULT_NORMALIZE_THRESHOLD, DEFAULT_NORMALIZE_HAND_MIN_WIDTH, DEFAULT_NORMALIZE_HAND_MIN_HEIGHT,
                DEFAULT_NORMALIZE_HAND_MAX_WIDTH, DEFAULT_NORMALIZE_HAND_MAX_HEIGHT, DEFAULT_EVENT_INTERVAL);
        
        HumanDetectFaceRequestParams face = new HumanDetectFaceRequestParams(new ArrayList<String>(),
                DEFAULT_NORMALIZE_THRESHOLD, DEFAULT_NORMALIZE_FACE_MIN_WIDTH, DEFAULT_NORMALIZE_FACE_MIN_HEIGHT,
                DEFAULT_NORMALIZE_FACE_MAX_WIDTH, DEFAULT_NORMALIZE_FACE_MAX_HEIGHT, DEFAULT_EVENT_INTERVAL);
        
        HumanDetectRequestParams requestParams = new HumanDetectRequestParams(body, hand, face);
        return requestParams;
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
        HvcBodyRequestParams bodyRequestParams = new HvcBodyRequestParams(mRequestParams.getBody());
        return bodyRequestParams;
    }
    
    /**
     * get HVC hand request parameters.
     * @return HVC hand request parameters.
     */
    public HvcHandRequestParams getHand() {
        HvcHandRequestParams handRequestParams = new HvcHandRequestParams(mRequestParams.getHand());
        return handRequestParams;
    }
    
    /**
     * get HVC face request parameters.
     * @return HVC face request parameters.
     */
    public HvcFaceRequestParams getFace() {
        HvcFaceRequestParams faceRequestParams = new HvcFaceRequestParams(mRequestParams.getFace());
        return faceRequestParams;
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
        
        hvcPrm.body.Threshold = body.getHvcThreshold();
        hvcPrm.body.MinSize = body.getHvcMinWidth();
        hvcPrm.body.MaxSize = body.getHvcMaxWidth();
        
        hvcPrm.hand.Threshold = hand.getHvcThreshold();
        hvcPrm.hand.MinSize = hand.getHvcMinWidth();
        hvcPrm.hand.MaxSize = hand.getHvcMaxWidth();
        
        hvcPrm.face.Threshold = face.getHvcThreshold();
        hvcPrm.face.MinSize = face.getHvcMinWidth();
        hvcPrm.face.MaxSize = face.getHvcMaxWidth();
        
        return hvcPrm;
    }
    
}
