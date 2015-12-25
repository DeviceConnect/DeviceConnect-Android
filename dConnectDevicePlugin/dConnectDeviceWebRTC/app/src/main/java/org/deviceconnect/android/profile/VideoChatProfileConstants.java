/*
 VideoChatProfileConstants.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import org.deviceconnect.profile.DConnectProfileConstants;

public interface VideoChatProfileConstants extends DConnectProfileConstants {

    /**
     * profile name.
     */
    String PROFILE_NAME = "videochat";

    /**
     * attribute : {@value} .
     */
    String ATTR_PROFILE = "profile";

    /**
     * attribute : {@value} .
     */
    String ATTR_ADDRESS = "address";

    /**
     * attribute : {@value} .
     */
    String ATTR_CALL = "call";

    /**
     * attribute : {@value} .
     */
    String ATTR_INCOMING = "incoming";

    /**
     * attribute : {@value} .
     */
    String ATTR_ONCALL = "oncall";

    /**
     * attribute : {@value} .
     */
    String ATTR_HANGUP = "hangup";

    /**
     * parameter : {@value} .
     */
    String PARAM_CONFIG = "config";

    /**
     * parameter : {@value} .
     */
    String PARAM_NAME = "name";

    /**
     * parameter : {@value} .
     */
    String PARAM_ADDRESSID = "addressId";

    /**
     * parameter : {@value} .
     */
    String PARAM_STATUS = "status";

    /**
     * parameter : {@value} .
     */
    String PARAM_GROUPID = "groupId";

    /**
     * parameter : {@value} .
     */
    String PARAM_VIDEO = "video";

    /**
     * parameter : {@value} .
     */
    String PARAM_AUDIO = "audio";

    /**
     * parameter : {@value} .
     */
    String PARAM_ADDRESSES = "addresses";

    /**
     * parameter : {@value} .
     */
    String PARAM_INCOMING = "incoming";

    /**
     * parameter : {@value} .
     */
    String PARAM_ONCALL = "oncall";

    /**
     * parameter : {@value} .
     */
    String PARAM_HANGUP = "hangup";

    /**
     * Video Chat状態を定義します.
     */
    enum State {
        /** State of idle. */
        IDLE("idle"),
        /** State of calling. */
        CALLING("calling"),
        /** State of talking. */
        TALKING("talking"),
        /** State of incoming. */
        INCOMING("incoming");

        /**
         * Value of state.
         */
        private String mValue;

        /**
         * 指定された状態を持つVideo Chat状態を定義します.
         *
         * @param value 定義値
         */
        private State(final String value) {
            this.mValue = value;
        }

        /**
         * 定義値を取得する.
         *
         * @return 定義値
         */
        public String getValue() {
            return mValue;
        }

        /**
         * 定義値から定数を取得する.
         *
         * @param value 定義値
         * @return 定数オブジェクト
         */
        public static State getInstance(final String value) {
            for (State state : values()) {
                if (state.getValue().equals(value)) {
                    return state;
                }
            }
            return IDLE;
        }
    }
}
