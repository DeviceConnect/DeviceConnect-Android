package org.deviceconnect.android.libmedia.streaming.rtsp.session.audio;

import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.audio.MicAACLATMEncoder;
import org.deviceconnect.android.libmedia.streaming.rtp.RtpPacketize;
import org.deviceconnect.android.libmedia.streaming.rtp.packet.AACLATMPacketize;
import org.deviceconnect.android.libmedia.streaming.sdp.MediaDescription;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.ControlAttribute;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.FormatAttribute;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.RtpMapAttribute;

public class MicAACLATMStream extends AudioStream {
    /**
     * デバッグ用フラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "MIC-AAC-STREAM";

    /**
     * ペイロードタイプ.
     */
    private static final int PAYLOAD_TYPE = 97;

    /**
     * 音声用のエンコーダ.
     */
    private AudioEncoder mAudioEncoder;

    /**
     * 音声のコンフィグ.
     */
    private String mAudioConfig;

    public MicAACLATMStream() {
        super();
        mAudioEncoder =  new MicAACLATMEncoder();
    }

    /**
     * コンストラクタ.
     *
     * @param port 送信先のポート番号
     */
    public MicAACLATMStream(int port) {
        this();
        setDestinationPort(port);
    }

    /**
     * サンプリングレートのインデックスを取得します.
     *
     * @return サンプリングレートのインデックス
     */
    private int getSamplingRateIndex() {
        int samplingRate = mAudioEncoder.getAudioQuality().getSamplingRate();
        int[] samplingRates = mAudioEncoder.getAudioQuality().getSupportSamplingRates();
        for (int i = 0; i < samplingRates.length; i++) {
            if (samplingRates[i] == samplingRate) {
                return i;
            }
        }
        return 0;
    }

    // MediaStream

    @Override
    public RtpPacketize createRtpPacketize() {
        AACLATMPacketize packetize = new AACLATMPacketize();
        packetize.setPayloadType(PAYLOAD_TYPE);
        packetize.setSamplingRate(mAudioEncoder.getAudioQuality().getSamplingRate());
        return packetize;
    }

    @Override
    public void configure() {
        if (mAudioConfig == null) {
            int profile = 2; // AAC は 2 なので固定
            int samplingRateIndex = getSamplingRateIndex();
            int channelCount = mAudioEncoder.getAudioQuality().getChannelCount();
            int config = (profile & 0x1F) << 11 | (samplingRateIndex & 0x0F) << 7 | (channelCount & 0x0F) << 3;

            mAudioConfig = Integer.toHexString(config);

            if (DEBUG) {
                Log.d(TAG, "### Audio Config " + mAudioConfig);
            }
        }
    }

    @Override
    public MediaDescription getMediaDescription() {
        AudioQuality quality = mAudioEncoder.getAudioQuality();

        // https://tools.ietf.org/html/rfc5691
        // https://tools.ietf.org/html/rfc6416

        // streamType:
        //  The integer value that indicates the type of MPEG-4 stream that is
        //  carried; its coding corresponds to the values of the streamType,
        //  as defined in Table 9 (streamType Values) in ISO/IEC 14496-1.
        //    0x00: reserved for IOS use
        //    0x01: Object Descriptor Stream
        //    0x02: Clock Descriptor Stream
        //    0x03: Scene Descriptor Stream
        //    0x04: Visual Stream
        //    0x05: Audio Stream
        //    0x06: MPEG-7 Stream
        //
        // profile-level-id:
        //  A decimal representation of the MPEG-4 Profile Level indication.
        //  This parameter MUST be used in the capability exchange or session
        //  set-up procedure to indicate the MPEG-4 Profile and Level
        //  combination of which the relevant MPEG-4 media codec is capable.
        //
        // The following are some examples of the "profile-level-id" value:
        //  1 : Main Audio Profile Level 1
        //  9 : Speech Audio Profile Level 1
        //  15: High Quality Audio Profile Level 2
        //  30: Natural Audio Profile Level 1
        //  44: High Efficiency AAC Profile Level 2
        //  48: High Efficiency AAC v2 Profile Level 2
        //  55: Baseline MPEG Surround Profile (see ISO/IEC 23003-1) Level 3

        FormatAttribute fmt = new FormatAttribute(PAYLOAD_TYPE);
        fmt.addParameter("streamtype", "5");
        fmt.addParameter("profile-level-id", "15");
        fmt.addParameter("mode", "AAC-hbr");
        fmt.addParameter("config", mAudioConfig);
        fmt.addParameter("SizeLength", "13");
        fmt.addParameter("IndexLength", "3");
        fmt.addParameter("IndexDeltaLength", "3");

        MediaDescription mediaDescription = new MediaDescription("audio", getDestinationPort(), "RTP/AVP", PAYLOAD_TYPE);
        mediaDescription.addAttribute(new RtpMapAttribute(PAYLOAD_TYPE, "mpeg4-generic", quality.getSamplingRate(), quality.getChannelCount()));
        mediaDescription.addAttribute(fmt);
        mediaDescription.addAttribute(new ControlAttribute("trackID=" + getTrackId()));
        return mediaDescription;
    }

    // AudioStream

    @Override
    public void setMute(boolean mute) {
        // ミュート設定にした時にエンコーダを再起動
        AudioEncoder audioEncoder = getAudioEncoder();
        if (audioEncoder != null) {
            audioEncoder.setMute(mute);
        }
    }

    @Override
    public AudioEncoder getAudioEncoder() {
        return mAudioEncoder;
    }
}
