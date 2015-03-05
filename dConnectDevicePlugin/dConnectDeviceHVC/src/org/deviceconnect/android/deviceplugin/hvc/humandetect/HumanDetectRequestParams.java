/*
 HumanDetectRequestParams.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.humandetect;


/**
 * Human Detect Request Parameters.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HumanDetectRequestParams {

    /**
     * body parameter.
     */
    private HumanDetectBodyRequestParams mBody;
    /**
     * hand parameter.
     */
    private HumanDetectHandRequestParams mHand;
    /**
     * face parameter.
     */
    private HumanDetectFaceRequestParams mFace;
    
    
    /**
     * Constructor(with body, hand, face).
     * @param body default body request parameters.
     * @param hand default hand request parameters.
     * @param face default face request parameters.
     */
    public HumanDetectRequestParams(final HumanDetectBodyRequestParams body, final HumanDetectHandRequestParams hand,
            final HumanDetectFaceRequestParams face) {
        mBody = body;
        mHand = hand;
        mFace = face;
    }
    
    /**
     * set body request parameters.
     * @param body body request parameters
     */
    public void setBody(final HumanDetectBodyRequestParams body) {
        mBody = body;
    }
    
    /**
     * set hand request parameters.
     * @param hand hand request parameters
     */
    public void setHand(final HumanDetectHandRequestParams hand) {
        mHand = hand;
    }
    
    /**
     * set face request parameters.
     * @param face face request parameters
     */
    public void setFace(final HumanDetectFaceRequestParams face) {
        mFace = face;
    }
    
    /**
     * get body request parameters.
     * @return body request parameters
     */
    public HumanDetectBodyRequestParams getBody() {
        return mBody;
    }
    
    /**
     * get hand request parameters.
     * @return hand request parameters
     */
    public HumanDetectHandRequestParams getHand() {
        return mHand;
    }
    
    /**
     * get face request parameters.
     * @return face request parameters
     */
    public HumanDetectFaceRequestParams getFace() {
        return mFace;
    }
}
