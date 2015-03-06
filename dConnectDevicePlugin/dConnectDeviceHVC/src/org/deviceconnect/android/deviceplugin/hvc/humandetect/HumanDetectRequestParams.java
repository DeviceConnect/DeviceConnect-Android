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
     * body parameter(if null, no use).
     */
    private HumanDetectBodyRequestParams mBody;
    /**
     * hand parameter(if null, no use).
     */
    private HumanDetectHandRequestParams mHand;
    /**
     * face parameter(if null, no use).
     */
    private HumanDetectFaceRequestParams mFace;
    /**
     * event parameter(if null, not event (GET/POST API)).
     */
    private HumanDetectEventRequestParams mEvent;
    
    
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
     * set event request parameters.
     * @param event event request parameters
     */
    public void setEvent(final HumanDetectEventRequestParams event) {
        mEvent = event;
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
    
    /**
     * get event request parameters.
     * @return event request parameters
     */
    public HumanDetectEventRequestParams getEvent() {
        return mEvent;
    }
}
