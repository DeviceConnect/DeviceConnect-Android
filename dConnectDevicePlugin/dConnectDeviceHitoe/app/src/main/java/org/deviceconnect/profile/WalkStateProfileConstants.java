/*
 WalkStateProfileConstants.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.profile;

/**
 * WalkState Profile API 定数群.<br/>
 * WalkState Profile API のパラメータ名、インタフェース名、属性名、プロファイル名を定義する。
 *
 * @author NTT DOCOMO, INC.
 */
public interface WalkStateProfileConstants extends DConnectProfileConstants {
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

    /**
     * プロファイル名: {@value} .
     */
    String PROFILE_NAME = "walkState";
    /**
     * 属性: {@value} .
     */
    String ATTRIBUTE_ON_WALK_STATE = "onWalkState";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_WALK = "walk";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_STEP = "step";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_STATE = "state";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_SPEED = "speed";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_DISTANCE = "distance";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_BALANCE = "balance";

    /**
     * パラメータ: {@value} .
     */
    String PARAM_TIMESTAMP = "timeStamp";
    /**
     * パラメータ: {@value} .
     */
    String PARAM_TIMESTAMP_STRING = "timeStampString";

}
