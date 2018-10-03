/*
 HVCCameraInfo
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcc2w.manager.data;

import android.content.Intent;
import android.util.Log;
import android.util.SparseArray;

import org.deviceconnect.android.profile.HumanDetectionProfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.omron.hvcw.OkaoResult;

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
    /** MAC Address. */
    private String mMacAddress;
    /** Application ID associated. */
    private String mAppId;
    /** Classification of owner. */
    private int mOwnerType;
    /** The owner of the e-mail address. */
    private String mOwnerEmail;
    /** Is Okao executing.*/
    private boolean mIsStarted;
    /** Body Detect Event Listener. */
    private OnBodyEventListener mBodyEvent;
    /** Hand Detect Event Listener. */
    private OnHandEventListener mHandEvent;
    /** Face Detect Event Listener. */
    private OnFaceEventListener mFaceEvent;
    /** Face Recognize Event Listener. */
    private OnFaceRecognizeEventListener mFaceRecogEvent;
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
     * Constructor.
     * @param id ID
     * @param name Name
     * @param macAddress Mac Address
     * @param appID Application ID
     * @param ownerType Classification of Owner
     * @param ownerEmail Owner of email address
     */
    public HVCCameraInfo(final String id, final String name, final String macAddress,
                         final String appID, final int ownerType, final String ownerEmail) {
        this.mId = id;
        this.mName = name;
        this.mMacAddress = macAddress;
        this.mAppId = appID;
        this.mOwnerType = ownerType;
        this.mOwnerEmail = ownerEmail;
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
     * Set MAC Address.
     * @param macAddress MAC Address
     */
    public void setMacAddress(String macAddress) {
        this.mMacAddress = macAddress;
    }

    /**
     * Get Mac Address.
     * @return MAC Address
     */
    public String getMacAddress() {
        return mMacAddress;
    }

    /**
     * Set Application ID.
     * @param appID Application ID
     */
    public void setAppID(String appID) {
        this.mAppId = appID;
    }

    /**
     * Get Application ID.
     * @return Application ID
     */
    public String getAppID() {
        return mAppId;
    }

    /**
     * Set OwnerType.
     * @param ownerType Owner type.
     */
    public void setOwnerType(int ownerType) {
        this.mOwnerType = ownerType;
    }

    /**
     * Get Owner Type.
     * @return Owner type
     */
    public int getOwnerType() {
        return mOwnerType;
    }

    /**
     * Set Owner's mail address.
     * @param ownerEmail owner's mail address
     */
    public void setOwnerEmail(String ownerEmail) {
        this.mOwnerEmail = ownerEmail;
    }

    /**
     * Get Owner's mail address
     * @return owner's mail address
     */
    public String getOwnerEmail() {
        return mOwnerEmail;
    }

    /**
     * Set isStarted.
     * @param isStarted Is Started
     */
    public void setIsStarted(final boolean isStarted) {
        mIsStarted = isStarted;
    }

    /**
     * Is Started Okao Event.
     * @return true:start false:stop
     */
    public boolean isStarted() {
        return mIsStarted;
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
     * @param bodyEvent
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
