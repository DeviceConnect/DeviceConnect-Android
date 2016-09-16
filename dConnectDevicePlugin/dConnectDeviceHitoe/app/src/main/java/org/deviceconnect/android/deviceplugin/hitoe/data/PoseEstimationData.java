/*
 PoseEstimationData
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.data;

import android.os.Bundle;

import org.deviceconnect.android.profile.PoseEstimationProfile;
import org.deviceconnect.profile.PoseEstimationProfileConstants;

/**
 * This class is information of Pose Estimation.
 * @author NTT DOCOMO, INC.
 */
public class PoseEstimationData {


    /** Pose state. */
    private PoseEstimationProfileConstants.PoseState mState = PoseEstimationProfileConstants.PoseState.Standing;
    /** Poses TimeStamp. */
    private long mTimeStamp;
    /** Poses TimeStamp String. */
    private String mTimeStampString;

    /**
     * Get Pose state.
     * @return Pose state
     */
    public PoseEstimationProfileConstants.PoseState getPoseState() {
        return mState;
    }

    /**
     * Set Pose state.
     * @param state Pose state
     */
    public void setPoseState(final PoseEstimationProfileConstants.PoseState state) {
        mState = state;
    }

    /**
     * Get TimeStamp.
     * @return TimeStamp
     */
    public long getTimeStamp() {
        return mTimeStamp;
    }

    /**
     * Set TimeStamp.
     * @param timeStamp TimeStamp
     */
    public void setTimeStamp(final long timeStamp) {
        mTimeStamp = timeStamp;
    }

    /**
     * Get TimeStamp String.
     * @return TimeStamp String
     */
    public String getTimeStampString() {
        return mTimeStampString;
    }

    /**
     * Set TimeStamp String.
     * @param timeStampString TimeStamp String
     */
    public void setTimeStampString(final String timeStampString) {
        mTimeStampString = timeStampString;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"state\": ").append(mState.getState()).append(", ");
        builder.append("\"timeStamp\": ").append(mTimeStamp).append(", ");
        builder.append("\"timeStampString\": ").append(mTimeStampString).append("} ");
        return builder.toString();
    }

    /**
     * To bundle.
     * @return bundle
     */
    public Bundle toBundle() {
        Bundle pose = new Bundle();
        PoseEstimationProfile.setState(pose, mState.getState());
        PoseEstimationProfile.setTimestamp(pose, mTimeStamp);
        PoseEstimationProfile.setTimestampString(pose, mTimeStampString);
        return pose;
    }

}
