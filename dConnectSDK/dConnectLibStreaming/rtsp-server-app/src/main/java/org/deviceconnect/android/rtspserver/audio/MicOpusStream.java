package org.deviceconnect.android.rtspserver.audio;

import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.rtp.RtpPacketize;
import org.deviceconnect.android.libmedia.streaming.rtp.packet.OpusPacketize;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.audio.AudioStream;
import org.deviceconnect.android.libmedia.streaming.sdp.MediaDescription;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.ControlAttribute;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.RtpMapAttribute;

public class MicOpusStream extends AudioStream {

    /**
     * ペイロードタイプ.
     */
    private static final int PAYLOAD_TYPE = 111;

    /**
     * 音声用のエンコーダ.
     */
    private AudioEncoder mAudioEncoder;

    public MicOpusStream() {
        mAudioEncoder = new MicOpusEncoder();
    }

    @Override
    public AudioEncoder getAudioEncoder() {
        return mAudioEncoder;
    }

    @Override
    public RtpPacketize createRtpPacketize() {
        OpusPacketize packetize = new OpusPacketize();
        packetize.setPayloadType(PAYLOAD_TYPE);
        packetize.setClockFrequency(mAudioEncoder.getAudioQuality().getSamplingRate());
        return packetize;
    }

    @Override
    public void configure() {

    }

    @Override
    public MediaDescription getMediaDescription() {
        AudioQuality quality = mAudioEncoder.getAudioQuality();

        MediaDescription mediaDescription = new MediaDescription("audio", getDestinationPort(), "RTP/AVP", PAYLOAD_TYPE);
        mediaDescription.addAttribute(new RtpMapAttribute(PAYLOAD_TYPE, "OPUS", quality.getSamplingRate(), "1"));
        mediaDescription.addAttribute(new ControlAttribute("trackID=" + getTrackId()));
        return mediaDescription;
    }

    @Override
    public void setMute(boolean mute) {

    }
}
