/*
 PeerOption.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.core;

import android.content.Context;

import org.webrtc.EglBase;
import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.List;

public class PeerOption {
    private boolean mVideoCallEnabled = true;
    private boolean mLoopback;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mVideoFps = 30;
    private int mVideoStartBitrate;
    private int mVideoFacing;
    private String mVideoCodec;
    private boolean mVideoCodecHwAcceleration;
    private int mAudioStartBitrate;
    private String mAudioCodec;
    private boolean mNoAudioProcessing;
    private boolean mCpuOveruseDetection;
    private List<String> mIceServer = new ArrayList<>();

    private VideoType mVideoType = VideoType.CAMERA;
    private String mVideoUri;
    private String mAudioUri;
    private VideoRenderer.Callbacks mRender;

    private AudioType mAudioType = AudioType.MICROPHONE;

    private EglBase mEglBase;

    private Context mContext;

    public boolean isNoAudioProcessing() {
        return mNoAudioProcessing;
    }

    public void setNoAudioProcessing(boolean noAudioProcessing) {
        mNoAudioProcessing = noAudioProcessing;
    }

    public boolean isLoopback() {
        return mLoopback;
    }

    public PeerOption setLoopback(boolean loopback) {
        mLoopback = loopback;
        return this;
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public PeerOption setVideoWidth(int videoWidth) {
        mVideoWidth = videoWidth;
        return this;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public PeerOption setVideoHeight(int videoHeight) {
        mVideoHeight = videoHeight;
        return this;
    }

    public int getVideoFps() {
        return mVideoFps;
    }

    public PeerOption setVideoFps(int videoFps) {
        mVideoFps = videoFps;
        return this;
    }

    public int getVideoFacing() {
        return mVideoFacing;
    }

    public void setVideoFacing(int videoFacing) {
        mVideoFacing = videoFacing;
    }

    public int getVideoStartBitrate() {
        return mVideoStartBitrate;
    }

    public PeerOption setVideoStartBitrate(int videoStartBitrate) {
        mVideoStartBitrate = videoStartBitrate;
        return this;
    }

    public String getVideoCodec() {
        return mVideoCodec;
    }

    public PeerOption setVideoCodec(String videoCodec) {
        mVideoCodec = videoCodec;
        return this;
    }

    public boolean isVideoCodecHwAcceleration() {
        return mVideoCodecHwAcceleration;
    }

    public PeerOption setVideoCodecHwAcceleration(boolean videoCodecHwAcceleration) {
        mVideoCodecHwAcceleration = videoCodecHwAcceleration;
        return this;
    }

    public int getAudioStartBitrate() {
        return mAudioStartBitrate;
    }

    public PeerOption setAudioStartBitrate(int audioStartBitrate) {
        mAudioStartBitrate = audioStartBitrate;
        return this;
    }

    public String getAudioCodec() {
        return mAudioCodec;
    }

    public PeerOption setAudioCodec(String audioCodec) {
        mAudioCodec = audioCodec;
        return this;
    }

    public boolean isCpuOveruseDetection() {
        return mCpuOveruseDetection;
    }

    public PeerOption setCpuOveruseDetection(boolean cpuOveruseDetection) {
        mCpuOveruseDetection = cpuOveruseDetection;
        return this;
    }

    public List<String> getIceServer() {
        return mIceServer;
    }

    public PeerOption addIceServer(String iceServer) {
        mIceServer.add(iceServer);
        return this;
    }

    public VideoType getVideoType() {
        return mVideoType;
    }

    public PeerOption setVideoType(VideoType videoType) {
        mVideoType = videoType;
        return this;
    }

    public String getVideoUri() {
        return mVideoUri;
    }

    public PeerOption setVideoUri(String videoUri) {
        mVideoUri = videoUri;
        return this;
    }

    public String getAudioUri() {
        return mAudioUri;
    }

    public PeerOption setAudioUri(String audioUri) {
        mAudioUri = audioUri;
        return this;
    }

    public AudioType getAudioType() {
        return mAudioType;
    }

    public void setAudioType(AudioType audioType) {
        mAudioType = audioType;
    }

    public VideoRenderer.Callbacks getRender() {
        return mRender;
    }

    public PeerOption setVideoRender(VideoRenderer.Callbacks render) {
        mRender = render;
        return this;
    }

    public Context getContext() {
        return mContext;
    }

    public PeerOption setContext(Context context) {
        mContext = context;
        return this;
    }

    public EglBase getEglBase() {
        return mEglBase;
    }

    public void setEglBase(EglBase eglBase) {
        mEglBase = eglBase;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("video: {");
        sb.append("uri: " + mVideoUri);
        sb.append(",");
        sb.append("width: " + mVideoWidth);
        sb.append(",");
        sb.append("height: " + mVideoHeight);
        sb.append(",");
        sb.append("fps: " + mVideoFps);
        sb.append("}");
        sb.append("audio: {");
        sb.append("uri: " + mAudioUri);
        sb.append(",");
        sb.append("NoAudioProcessing: " + mNoAudioProcessing);
        sb.append("}");
        sb.append("}");
        return sb.toString();
    }

    public enum FacingType {
        BACK,
        FRONT
    }

    /**
     * Video Type.
     */
    public enum VideoType {
        NONE(0),
        /**
         * Defined a type of camera.
         */
        CAMERA(1),
        /**
         * Defined a type of external resource.
         */
        EXTERNAL_RESOURCE(2);

        /**
         * Video Type.
         */
        private int mType;

        /**
         * Constructor.
         * @param type video type
         */
        VideoType(final int type) {
            mType = type;
        }

        /**
         * Get a video type.
         * @return video type
         */
        public int getType() {
            return mType;
        }

        /**
         * Get a VideoType instance from value.
         * @param type video type
         * @return VideoType
         */
        public static VideoType valueOf(final int type) {
            for (VideoType t : values()) {
                if (t.getType() == type) {
                    return t;
                }
            }
            return null;
        }
    }

    /**
     * Audio Type.
     */
    public enum AudioType {
        NONE(0),
        /**
         * Defined a type of microphone.
         */
        MICROPHONE(1),
        /**
         * Defined a type of external resource.
         */
        EXTERNAL_RESOURCE(2);

        /**
         * Video Type.
         */
        private int mType;

        /**
         * Constructor.
         * @param type audio type
         */
        AudioType(final int type) {
            mType = type;
        }

        /**
         * Get a audio type.
         * @return audio type
         */
        public int getType() {
            return mType;
        }

        /**
         * Get a AudioType instance from value.
         * @param type video type
         * @return AudioType
         */
        public static AudioType valueOf(final int type) {
            for (AudioType t : values()) {
                if (t.getType() == type) {
                    return t;
                }
            }
            return null;
        }
    }
}
