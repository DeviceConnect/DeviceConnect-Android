/*
 HVCCameraInfo
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcp.manager.data;

import android.content.Intent;
import android.util.SparseArray;

import org.deviceconnect.android.profile.HumanDetectionProfile;

import java.util.List;

/**
 * HVC Camera Info.
 * @author NTT DOCOMO, INC.
 */
public final class HVCCameraInfo {
    /**
     * Threshold kind.
     */
    public enum ThresholdKind {
        EYE,
        NOSE,
        MOUTH,
        BLINK,
        AGE,
        GENDER,
        FACEDIRECTION,
        GAZE,
        EXPRESSION
    };
    /** ID. */
    private String mId;
    /** Name. */
    private String mName;
    /** Body Detect Event Listener. */
    private OnBodyEventListener mBodyEvent;
    /** Hand Detect Event Listener. */
    private OnHandEventListener mHandEvent;
    /** Face Detect Event Listener. */
    private OnFaceEventListener mFaceEvent;
    /** Face Recognize Event Listener. */
    private OnFaceRecognizeEventListener mFaceRecogEvent;
    /** One shot body get listener. */
    private OneShotOkaoResultResoponseListener mBodyGet;
    /** One shot hand get listener. */
    private OneShotOkaoResultResoponseListener mHandGet;
    /** One shot face get listener. */
    private OneShotOkaoResultResoponseListener mFaceGet;
    /** One shot set threshold listener. */
    private OneShotSetParameterResoponseListener mThresholdSet;
    /** One shot set size listener. */
    private OneShotSetParameterResoponseListener mSizeSet;
    /** Human detect Profile's Options. */
    private List<String> mOptions;
    /** FaceDetection Thresholds.*/
    private SparseArray<Double> mThresholds;

    /**
     * Face Evnet Listener.
     */
    public interface OnFaceEventListener {
        /**
         * Notify Okao Result.
         * @param serviceId service ID(cameraID)
         * @param result Okao result
         */
        void onNotifyForFaceDetectResult(final String serviceId, final OkaoResult result);
    }

    /**
     * Body Evnet Listener.
     */
    public interface OnBodyEventListener {
        /**
         * Notify Okao Result.
         * @param serviceId service ID(cameraID)
         * @param result Okao result
         */
        void onNotifyForBodyDetectResult(final String serviceId, final OkaoResult result);
    }

    /**
     * Hand Evnet Listener.
     */
    public interface OnHandEventListener {
        /**
         * Notify Okao Result.
         * @param serviceId service ID(cameraID)
         * @param result Okao result
         */
        void onNotifyForHandDetectResult(final String serviceId, final OkaoResult result);
    }

    /**
     * Face Recognize Evnet Listener.
     */
    public interface OnFaceRecognizeEventListener {
        /**
         * Notify Okao Result.
         * @param serviceId service ID(cameraID)
         * @param result Okao result
         */
        void onNotifyForFaceRecognizeResult(final String serviceId, final OkaoResult result);
    }

    /**
     * Okao Result one shot Response listener.
     */
    public interface OneShotOkaoResultResoponseListener {
        /**
         * Callback.
         * @param serviceId serviceID
         * @param result Okao Result
         */
        void onResponse(final String serviceId, final OkaoResult result);
    }
    /**
     * Set Parameter  Response listener.
     */
    public interface OneShotSetParameterResoponseListener {
        /**
         * Callback.
         * @param resultCode result code
         */
        void onResponse(final int resultCode);
    }
    /**
     * Constructor.
     * @param id ID
     * @param name Name
     */
    public HVCCameraInfo(final String id, final String name) {
        this.mId = id;
        this.mName = name;
        this.mBodyEvent = null;
        this.mFaceEvent = null;
        this.mHandEvent = null;
        this.mFaceRecogEvent = null;
        this.mThresholds = new SparseArray<>();
    }

    /**
     * Set ID.
     * @param id ID
     */
    public void setID(String id) {
        this.mId = id;
    }

    /**
     * Get ID.
     * @return ID
     */
    public String getID() {
        return mId;
    }

    /**
     * Set name.
     * @param name name
     */
    public void setName(String name) {
        this.mName = name;
    }

    /**
     * Get name.
     * @return name
     */
    public String getName() {
        return mName;
    }

      /**
     *  Body Event Listener.
     *  @return Body Event Listener
     *  */
    public OnBodyEventListener getBodyEvent() {
        return mBodyEvent;
    }

    /**
     * Set Body DetectEvent Listener.
     * @param bodyEvent body event listener
     */
    public void setBodyEvent(final OnBodyEventListener bodyEvent) {
        mBodyEvent = bodyEvent;
    }

    /**
     *  Get Hand Detect Event Listener.
     * @return Hand Event Listener
     * */
    public OnHandEventListener getHandEvent() {
        return mHandEvent;
    }

    /**
     * Set Hand Detect Event Listener.
     * @param handEvent Hand Event Listener
     */
    public void setHandEvent(final OnHandEventListener handEvent) {
        mHandEvent = handEvent;
    }

    /**
     * Get Face Detect Event Listener.
     * @return Face Detect Event Listener
     */
    public OnFaceEventListener getFaceEvent() {
        return mFaceEvent;
    }

    /**
     * Set Face Detect Event Listener
     * @param faceEvent Face Detect Event Listener
     */
    public void setFaceEvent(final OnFaceEventListener faceEvent) {
        mFaceEvent = faceEvent;
    }

    /**
     * Get Face Recognize Event Listener.
     * @return Face Recognize Event Listener
     */
    public OnFaceRecognizeEventListener getFaceRecognizeEvent() {
        return mFaceRecogEvent;
    }

    /**
     * Set Face Recognize Event Listener.
     * @param faceRecogEvent Face Recognize Event Listener
     */
    public void setFaceRecognizeEvent(final OnFaceRecognizeEventListener faceRecogEvent) {
        mFaceRecogEvent = faceRecogEvent;
    }
    /**
     * Get One shot okao result body listener.
     * @return One shot okao result body listener
     */
    public OneShotOkaoResultResoponseListener getBodyGet() {
        return mBodyGet;
    }

    /**
     * Set One shot okao result body listener.
     * @param bodyGet one shot okao result body listener
     */
    public void setBodyGet(final OneShotOkaoResultResoponseListener bodyGet) {
        this.mBodyGet = bodyGet;
    }

    /**
     * Get one shot okao result hand listener.
     * @return one shot okao result hand listener
     */
    public OneShotOkaoResultResoponseListener getHandGet() {
        return mHandGet;
    }

    /**
     * Set one shot okao result hand listener.
     * @param handGet one shot okao result hand listener
     */
    public void setHandGet(final OneShotOkaoResultResoponseListener handGet) {
        this.mHandGet = handGet;
    }

    /**
     * Get one shot okao result face listener.
     * @return one shot okao result face listener
     */
    public OneShotOkaoResultResoponseListener getFaceGet() {
        return mFaceGet;
    }

    /**
     * Set one shot okao result face listener.
     * @param faceGet one shot okao result face listener
     */
    public void setFaceGet(final OneShotOkaoResultResoponseListener faceGet) {
        this.mFaceGet = faceGet;
    }

    /**
     * Get one shot set threshold listener.
     * @return one shot set threshold listener
     */
    public OneShotSetParameterResoponseListener getThresholdSet() {
        return mThresholdSet;
    }

    /**
     * Set one shot set threshold listener.
     * @param thresholdSet one shot set threshold listener
     */
    public void setThresholdSet(final OneShotSetParameterResoponseListener thresholdSet) {
        this.mThresholdSet = thresholdSet;
    }

    /**
     * Get one shot set size listener.
     * @return one shot set size listener
     */
    public OneShotSetParameterResoponseListener getSizeSet() {
        return mSizeSet;
    }

    /**
     * Set one shot set size listener.
     * @param sizeSet one shot set size listener
     */
    public void setSizeSet(final OneShotSetParameterResoponseListener sizeSet) {
        this.mSizeSet = sizeSet;
    }


    /**
     * Get Human Detect Profile's options.
     * @return options
     */
    public List<String> getOptions() {
        return mOptions;
    }

    /**
     * Set Human Detect Profile's options.
     * @param options options
     */
    public void setOptions(final List<String> options) {
        mOptions = options;
    }
    /**
     * Set Face's threshold.
     * @param request request parameter
     * @throws NumberFormatException
     */
    public void setThresholds(final Intent request) throws NumberFormatException {
        mThresholds.put(ThresholdKind.EYE.ordinal(), HumanDetectionProfile.getEyeThreshold(request));
        mThresholds.put(ThresholdKind.NOSE.ordinal(), HumanDetectionProfile.getNoseThreshold(request));
        mThresholds.put(ThresholdKind.MOUTH.ordinal(), HumanDetectionProfile.getMouthThreshold(request));
        mThresholds.put(ThresholdKind.BLINK.ordinal(), HumanDetectionProfile.getBlinkThreshold(request));
        mThresholds.put(ThresholdKind.AGE.ordinal(), HumanDetectionProfile.getAgeThreshold(request));
        mThresholds.put(ThresholdKind.GENDER.ordinal(), HumanDetectionProfile.getGenderThreshold(request));
        mThresholds.put(ThresholdKind.FACEDIRECTION.ordinal(), HumanDetectionProfile.getFaceDirectionThreshold(request));
        mThresholds.put(ThresholdKind.GAZE.ordinal(), HumanDetectionProfile.getGazeThreshold(request));
        mThresholds.put(ThresholdKind.EXPRESSION.ordinal(), HumanDetectionProfile.getExpressionThreshold(request));
    }

    /**
     * Get Face's threshold.
     * @return Face's threshold.
     */
    public SparseArray<Double> getThresholds() {
        return mThresholds;
    }
}
