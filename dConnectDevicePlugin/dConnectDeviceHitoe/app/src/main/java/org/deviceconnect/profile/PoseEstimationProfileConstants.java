/*
 PoseEstimationProfileConstants.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.profile;

/**
 * PoseEstimation Profile API 定数群.<br/>
 * PoseEstimation Profile API のパラメータ名、インタフェース名、属性名、プロファイル名を定義する。
 *
 * @author NTT DOCOMO, INC.
 */
public interface PoseEstimationProfileConstants extends DConnectProfileConstants {
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
    /**
     * プロファイル名: {@value} .
     */
    String PROFILE_NAME = "poseEstimation";
    /**
     * 属性: {@value} .
     */
    String ATTRIBUTE_ON_POSE_ESTIMATION = "onPoseEstimation";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_STRESS = "pose";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_STATE = "state";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_TIMESTAMP = "timeStamp";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_TIMESTAMP_STRING = "timeStampString";

}
