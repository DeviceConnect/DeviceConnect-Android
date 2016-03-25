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

    private AudioSampleRate mAudioSampleRate = AudioSampleRate.RATE_48000;
    private AudioBitDepth mAudioBitDepth = AudioBitDepth.PCM_FLOAT;
    private AudioChannel mAudioChannel = AudioChannel.MONAURAL;

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

    public AudioSampleRate getAudioSampleRate() {
        return mAudioSampleRate;
    }

    public void setAudioSampleRate(AudioSampleRate audioSampleRate) {
        mAudioSampleRate = audioSampleRate;
    }

    public AudioChannel getAudioChannel() {
        return mAudioChannel;
    }

    public void setAudioChannel(AudioChannel audioChannel) {
        mAudioChannel = audioChannel;
    }

    public AudioBitDepth getAudioBitDepth() {
        return mAudioBitDepth;
    }

    public void setAudioBitDepth(AudioBitDepth audioBitDepth) {
        mAudioBitDepth = audioBitDepth;
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

    /**
     * Audio Sample Rate.
     */
    public enum AudioSampleRate {
        NONE(0),
        /**
         * Defined a type of 22050Hz.
         */
        RATE_22050(22050),
        /**
         * Defined a type of 32000Hz.
         */
        RATE_32000(32000),
        /**
         * Defined a type of 44100Hz.
         */
        RATE_44100(44100),
        /**
         * Defined a type of 48000Hz.
         */
        RATE_48000(48000);

        /**
         * SampleRate.
         */
        private int mSampleRate;

        /**
         * Constructor.
         * @param sampleRate sampleRate.
         */
        AudioSampleRate(final int sampleRate) {
            mSampleRate = sampleRate;
        }

        /**
         * Get a audio sample rate.
         * @return samplerate.
         */
        public int getSampleRate() {
            return mSampleRate;
        }

        /**
         * Get a Sample rate instance from value.
         * @param sampleRate sampleRate.
         * @return sampleRate
         */
        public static AudioSampleRate valueOf(final int sampleRate) {
            for (AudioSampleRate t : values()) {
                if (t.getSampleRate() == sampleRate) {
                    return t;
                }
            }
            return null;
        }
    }

    /**
     * Audio Bit Depth.
     */
    public enum AudioBitDepth {
        NONE(0),
        /**
         * Defined a type of 8bit.
         */
        PCM_8BIT(8),
        /**
         * Defined a type of 16bit.
         */
        PCM_16BIT(16),
        /**
         * Defined a type of float.
         */
        PCM_FLOAT(32);

        /**
         * BitDepth.
         */
        private int mBitDepth;

        /**
         * Constructor.
         * @param bitDepoth bitDepth.
         */
        AudioBitDepth(final int bitDepoth) {
            mBitDepth = bitDepoth;
        }

        /**
         * Get a audio bit depth.
         * @return bit depth.
         */
        public int getBitDepth() {
            return mBitDepth;
        }

        /**
         * Get a Bit depth instance from value.
         * @param bitDepoth bitDepoth.
         * @return bitDepoth
         */
        public static AudioBitDepth valueOf(final int bitDepoth) {
            for (AudioBitDepth t : values()) {
                if (t.getBitDepth() == bitDepoth) {
                    return t;
                }
            }
            return null;
        }
    }

    /**
     * Audio Channel.
     */
    public enum AudioChannel {
        NONE(0),
        /**
         * Defined a type of Monaural.
         */
        MONAURAL(1),
        /**
         * Defined a type of Stereo.
         */
        STEREO(2);

        /**
         * Channel.
         */
        private int mChannel;

        /**
         * Constructor.
         * @param channel channel.
         */
        AudioChannel(final int channel) {
            mChannel = channel;
        }

        /**
         * Get a audio channel.
         * @return channel.
         */
        public int getChannel() {
            return mChannel;
        }

        /**
         * Get a Channel instance from value.
         * @param channel channel.
         * @return Channel
         */
        public static AudioChannel valueOf(final int channel) {
            for (AudioChannel t : values()) {
                if (t.getChannel() == channel) {
                    return t;
                }
            }
            return null;
        }
    }
}
