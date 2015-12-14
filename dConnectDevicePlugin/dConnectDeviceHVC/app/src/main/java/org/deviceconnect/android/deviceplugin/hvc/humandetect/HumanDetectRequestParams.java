/*
 HumanDetectRequestParams.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.humandetect;

import java.util.List;

import android.util.Log;


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

    /**
     * output dump log.
     * @param tag tag
     */
    public void dumpLog(final String tag) {
        Log.d(tag, "------------------------------");
        Log.d(tag, "<" + HumanDetectRequestParams.class.getSimpleName() + ">");
        if (mBody == null) {
            Log.d(tag, "[body] null");
        } else {
            Log.d(tag, "[body] threshold:" + mBody.getThreshold()
                    + " minWidth:" + mBody.getMinWidth() + " maxWidth:" + mBody.getMaxWidth()
                    + " minHeight:" + mBody.getMinHeight() + " maxHeight:" + mBody.getMaxHeight());
        }
        if (mHand == null) {
            Log.d(tag, "[hand] null");
        } else {
            Log.d(tag, "[hand] threshold:" + mHand.getThreshold()
                    + " minWidth:" + mHand.getMinWidth() + " maxWidth:" + mHand.getMaxWidth()
                    + " minHeight:" + mHand.getMinHeight() + " maxHeight:" + mHand.getMaxHeight());
        }
        if (mFace == null) {
            Log.d(tag, "[face] null");
        } else {
            Log.d(tag, "[face] threshold:" + mFace.getThreshold()
                    + " minWidth:" + mFace.getMinWidth() + " maxWidth:" + mFace.getMaxWidth()
                    + " minHeight:" + mFace.getMinHeight() + " maxHeight:" + mFace.getMaxHeight());
            final String separator = ",";
            List<String> options = mFace.getOptions();
            StringBuilder sb = new StringBuilder();
            for (String option : options) {
                if (sb.length() > 0) {
                    sb.append(separator);
                }
                sb.append(option);
            }
            Log.d(tag, "[face] options:" + sb.toString());
            Log.d(tag, "[face] eyeThreshold:" + mFace.getEyeThreshold());
            Log.d(tag, "[face] noseThreshold:" + mFace.getNoseThreshold());
            Log.d(tag, "[face] mouthThreshold:" + mFace.getMouthThreshold());
            Log.d(tag, "[face] blinkThreshold:" + mFace.getBlinkThreshold());
            Log.d(tag, "[face] ageThreshold:" + mFace.getAgeThreshold());
            Log.d(tag, "[face] genderThreshold:" + mFace.getGenderThreshold());
            Log.d(tag, "[face] gazeThreshold:" + mFace.getGazeThreshold());
            Log.d(tag, "[face] expressionThreshold:" + mFace.getExpressionThreshold());
            
        }
        if (mEvent == null) {
            Log.d(tag, "[event] null");
        } else {
            Log.d(tag, "[event] interval:" + mEvent.getInterval());
        }
        Log.d(tag, "------------------------------");
    }
}
