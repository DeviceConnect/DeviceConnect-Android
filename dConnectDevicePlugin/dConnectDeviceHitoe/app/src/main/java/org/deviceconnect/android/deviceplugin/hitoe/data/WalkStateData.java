/*
 WalkStateData
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.data;

import android.os.Bundle;

import org.deviceconnect.android.profile.WalkStateProfile;
import org.deviceconnect.profile.WalkStateProfileConstants;

/**
 * This class is information of WalkState.
 * @author NTT DOCOMO, INC.
 */
public class WalkStateData {

    /** Step. */
    private int mStep;
    /** Walk state. */
    private WalkStateProfileConstants.WalkState mState = WalkStateProfileConstants.WalkState.Stop;
    /** Walk speed. */
    private double mSpeed;
    /** Walk distance. */
    private double mDistance;
    /** Walk balance. */
    private double mBalance;
    /** Walk TimeStamp. */
    private long mTimeStamp;
    /** Walk TimeStamp String. */
    private String mTimeStampString;

    /**
     * Get Walk step count.
     * @return Walk step count
     */
    public int getStep() {
        return mStep;
    }

    /**
     * Set Walk step count.
     * @param step Walk step count
     */
    public void setStep(final int step) {
        mStep = step;
    }

    /**
     * Get Walk state.
     * @return Walk state
     */
    public WalkStateProfileConstants.WalkState getState() {
        return mState;
    }

    /**
     * Set Walk state.
     * @param state Walk state
     */
    public void setState(final WalkStateProfileConstants.WalkState state) {
        mState = state;
    }

    /**
     * Get walk speed.
     * @return walk speed(km/s)
     */
    public double getSpeed() {
        return mSpeed;
    }

    /**
     * Set walk speed.
     * @param speed walk speed
     */
    public void setSpeed(final double speed) {
        mSpeed = speed;
    }

    /**
     * Get walk distance.
     * @return walk distance
     */
    public double getDistance() {
        return mDistance;
    }

    /**
     * Set walk distance.
     * @param distance walk distance
     */
    public void setDistance(final double distance) {
        mDistance = distance;
    }

    /**
     * Get walk balance.
     * @return walk balance
     */
    public double getBalance() {
        return mBalance;
    }

    /**
     * Set walk balance.
     * @param balance walk balance
     */
    public void setBalance(final double balance) {
        mBalance = balance;
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
        this.mTimeStamp = timeStamp;
    }

    /**
     * Get TimeStamp String.
     * @return TimeStamp String
     */
    public String getTimeStampString() {
        return mTimeStampString;
    }

    /**
     * Set TimeStamp string.
     * @param timeStampString TimeStamp string
     */
    public void setTimeStampString(final String timeStampString) {
        mTimeStampString = timeStampString;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"steps\": ").append(mStep).append(", ");
        builder.append("\"state\": ").append(mState.getState()).append(", ");
        builder.append("\"speed\": ").append(mSpeed).append(", ");
        builder.append("\"distance\": ").append(mDistance).append(", ");
        builder.append("\"balance\": ").append(mBalance).append(", ");
        builder.append("\"timeStamp\": ").append(mTimeStamp).append(", ");
        builder.append("\"timeStampString\": ").append(mTimeStampString).append("} ");
        return builder.toString();
    }

    /**
     * To bundle.
     * @return bundle
     */
    public Bundle toBundle() {
        Bundle walk = new Bundle();
        WalkStateProfile.setStep(walk, mStep);
        WalkStateProfile.setState(walk, mState.getState());
        WalkStateProfile.setSpeed(walk, mSpeed);
        WalkStateProfile.setDistance(walk, mDistance);
        WalkStateProfile.setBalance(walk, mBalance);
        WalkStateProfile.setTimestamp(walk, mTimeStamp);
        WalkStateProfile.setTimestampString(walk, mTimeStampString);
        return walk;
    }

}
