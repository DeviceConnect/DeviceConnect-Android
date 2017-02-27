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
    String PROFILE_NAME = "videoChat";

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
    String ATTR_ONINCOMING = "onIncoming";

    /**
     * attribute : {@value} .
     */
    String ATTR_ONCALL = "onCall";

    /**
     * attribute : {@value} .
     */
    String ATTR_ONHANGUP = "onHangUp";

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
    String PARAM_OUTPUTS = "outputs";

    /**
     * parameter : {@value} .
     */
    String PARAM_AUDIOSAMPLERATE = "audioSampleRate";

    /**
     * parameter : {@value} .
     */
    String PARAM_AUDIOBITDEPTH = "audioBitDepth";

    /**
     * parameter : {@value} .
     */
    String PARAM_AUDIOCHANNEL = "audioChannel";

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
     * parameter : {@value} .
     */
    String PARAM_APP = "app";

    /**
     * parameter : {@value} .
     */
    String PARAM_HOST = "host";

    /**
     * parameter : {@value} .
     */
    String PARAM_LOCAL = "local";

    /**
     * parameter : {@value} .
     */
    String PARAM_REMOTE = "remote";

    /**
     * parameter : {@value} .
     */
    String PARAM_MIMETYPE = "mimeType";

    /**
     * parameter : {@value} .
     */
    String PARAM_FRAMERATE = "frameRate";

    /**
     * parameter : {@value} .
     */
    String PARAM_WIDTH = "width";

    /**
     * parameter : {@value} .
     */
    String PARAM_HEIGHT = "height";

    /**
     * parameter : {@value} .
     */
    String PARAM_SAMPLERATE = "sampleRate";

    /**
     * parameter : {@value} .
     */
    String PARAM_CHANNELS = "channels";

    /**
     * parameter : {@value} .
     */
    String PARAM_SAMPLESIZE = "sampleSize";

    /**
     * parameter : {@value} .
     */
    String PARAM_BLOCKSIZE = "blockSize";

    /**
     * parameter : {@value} .
     */
    int PARAM_RATE_22050 = 22050;

    /**
     * parameter : {@value} .
     */
    int PARAM_RATE_32000 = 32000;

    /**
     * parameter : {@value} .
     */
    int PARAM_RATE_44100 = 44100;

    /**
     * parameter : {@value} .
     */
    int PARAM_RATE_48000 = 48000;

    /**
     * parameter : {@value} .
     */
    String PARAM_PCM_8BIT = "PCM_8BIT";

    /**
     * parameter : {@value} .
     */
    String PARAM_PCM_16BIT = "PCM_16BIT";

    /**
     * parameter : {@value} .
     */
    String PARAM_PCM_FLOAT = "PCM_FLOAT";

    /**
     * parameter : {@value} .
     */
    String PARAM_MONAURAL = "Monaural";

    /**
     * parameter : {@value} .
     */
    String PARAM_STEREO = "Stereo";

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
        State(final String value) {
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
