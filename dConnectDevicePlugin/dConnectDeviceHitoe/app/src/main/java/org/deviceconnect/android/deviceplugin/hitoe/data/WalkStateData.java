/*
 WalkStateData
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.data;

/**
 * This class is information of WalkState.
 * @author NTT DOCOMO, INC.
 */
public class WalkStateData {
    /** Walk State enum. */
    public enum WalkState {
        /** Stop. */
        Stop("Stop"),
        /** Walking. */
        Walking("Walking"),
        /** Running. */
        Running("Running");

        /**
         * Text.
         */
        private final String mState;

        /**
         * Constructor.
         * @param state State
         */
        private WalkState(final String state) {
            mState = state;
        }

        /**
         * Get Walk State.
         * @return Walk state
         */
        public String getState() {
            return mState;
        }
    }

    /** Step. */
    private int mStep;
    /** Walk state. */
    private WalkState mState;
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
    public WalkState getState() {
        return mState;
    }

    /**
     * Set Walk state.
     * @param state Walk state
     */
    public void setState(final WalkState state) {
        mState = state;
    }

    /**
     * Get walk speed.
     * @return
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
        builder.append("{\"steps\": " + mStep + ", ");
        builder.append("\"state\": " + mState.getState() + ", ");
        builder.append("\"speed\": " + mSpeed + ", ");
        builder.append("\"distance\": " + mDistance + ", ");
        builder.append("\"balance\": " + mBalance + ", ");
        builder.append("\"timeStamp\": " + mTimeStamp + ", ");
        builder.append("\"timeStampString\": " + mTimeStampString +  "} ");
        return builder.toString();
    }

    /**
     * Notify walk state listener.
     */
    public interface OnWalkStateListener {
        /**
         * Notify walk state.
         * @param device Hitoe device
         * @param notify walk state data
         */
        void onNotifyWalkStateData(final HitoeDevice device, final WalkStateData notify);
    }
}
