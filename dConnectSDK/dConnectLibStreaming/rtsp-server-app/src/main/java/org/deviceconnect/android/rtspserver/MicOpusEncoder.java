package org.deviceconnect.android.rtspserver;

import android.media.MediaCodec;

import org.deviceconnect.opuscodec.MicOpusRecorder;
import org.deviceconnect.opuscodec.OpusConstants;
import org.deviceconnect.opuscodec.OpusEncoder;

import java.nio.ByteBuffer;

import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;

public class MicOpusEncoder extends AudioEncoder {
    /**
     * マイクから音声を入力するためのスレッド.
     */
    private MicOpusRecorder mMicOpusRecorder;

    @Override
    public AudioQuality getAudioQuality() {
        return new AudioQuality("audio/opus") {
            @Override
            public int[] getSupportSamplingRates() {
                return new int[] {
                        8000,
                        16000,
                        24000,
                        48000
                };
            }
        };
    }

    @Override
    protected void prepare() {
        // Opus は MediaCodec を使用しないので、ここではオーバーライドして
        // super.prepare() を呼び出しません。
    }

    @Override
    protected void startRecording() {
        super.startRecording();

        OpusEncoder.SamplingRate samplingRate = OpusConstants.SamplingRate.samplingRateOf(
                getAudioQuality().getSamplingRate());
        OpusEncoder.FrameSize frameSize = OpusEncoder.FrameSize.E_20_MS;
        int channels = getAudioQuality().getChannelCount();
        mMicOpusRecorder = new MicOpusRecorder(samplingRate, channels,
                frameSize, OpusConstants.BITRATE_MAX, OpusEncoder.Application.E_AUDIO,
                mAudioRecordCallback);
        mMicOpusRecorder.start();
    }

    @Override
    protected void stopRecording() {
        if (mMicOpusRecorder != null) {
            mMicOpusRecorder.stop();
            mMicOpusRecorder = null;
        }

        super.stopRecording();
    }

    @Override
    protected void release() {
        // Opus は MediaCodec を使用しないので、ここではオーバーライドして
        // super.release() を呼び出しません。
    }

    private MicOpusRecorder.AudioRecordCallback mAudioRecordCallback = new MicOpusRecorder.AudioRecordCallback() {
        private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
        private ByteBuffer mBuffer = ByteBuffer.allocate(4096);

        @Override
        public void onPeriodicNotification(byte[] bytes, int byteLength) {
            mBuffer.clear();
            mBuffer.put(bytes);
            mBuffer.limit(byteLength);
            mBuffer.position(0);

            mBufferInfo.offset = 0;
            mBufferInfo.size = byteLength;
            mBufferInfo.presentationTimeUs = getPTSUs();

            postOnWriteData(mBuffer, mBufferInfo);
        }

        @Override
        public void onEncoderError() {
            postOnError(new MediaEncoderException("OpusEncoder"));
        }
    };
}
