/*
 PoseEstimationData
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.data;

/**
 * This class is information of Pose Estimation.
 * @author NTT DOCOMO, INC.
 */
public class PoseEstimationData {
    /** Pose State enum. */
    public enum PoseState {
        /** Forward. */
        Forward("Forward"),
        /** Backward. */
        Backward("Backward"),
        /** Rightside. */
        Rightside("Rightside"),
        /** Leftside. */
        Leftside("Leftside"),
        /** FaceUp. */
        FaceUp("FaceUp"),
        /** FaceLeft. */
        FaceLeft("FaceLeft"),
        /** FaceDown. */
        FaceDown("FaceDown"),
        /** FaceRight. */
        FaceRight("FaceRight"),
        /** Standing. */
        Standing("Standing");

        /**
         * Text.
         */
        private final String mState;

        /**
         * Constructor.
         * @param state State
         */
        private PoseState(final String state) {
            mState = state;
        }

        /**
         * Get Pose State.
         * @return Pose state
         */
        public String getState() {
            return mState;
        }
    }


    /** Pose state. */
    private PoseState mState;
    /** Poses TimeStamp. */
    private long mTimeStamp;
    /** Poses TimeStamp String. */
    private String mTimeStampString;

    /**
     * Get Pose state.
     * @return Pose state
     */
    public PoseState getPoseState() {
        return mState;
    }

    /**
     * Set Pose state.
     * @param state Pose state
     */
    public void setPoseState(final PoseState state) {
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
        builder.append("{\"state\": " + mState.getState() + ", ");
        builder.append("\"timeStamp\": " + mTimeStamp + ", ");
        builder.append("\"timeStampString\": " + mTimeStampString +  "} ");
        return builder.toString();
    }




    /**
     * Notify pose estimation data listener.
     */
    public interface OnPoseEstimationDataListener {
        /**
         * Notify pose estimation data.
         * @param device hitoe device
         * @param notify pose estimation data
         */
        void onNotifyPoseEstimationData(final HitoeDevice device, final PoseEstimationData notify);
    }
}
