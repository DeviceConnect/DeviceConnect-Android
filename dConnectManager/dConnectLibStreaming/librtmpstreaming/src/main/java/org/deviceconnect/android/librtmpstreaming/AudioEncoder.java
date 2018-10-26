package org.deviceconnect.android.librtmpstreaming;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import com.pedro.encoder.utils.CodecUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.media.MediaFormat.KEY_BIT_RATE;
import static android.media.MediaFormat.KEY_CHANNEL_COUNT;
import static android.media.MediaFormat.KEY_SAMPLE_RATE;

public class AudioEncoder {

    public interface Callback {
        void onAudioFormat(MediaFormat mediaFormat);
        void onReceiveAacData(ByteBuffer aacBytes, MediaCodec.BufferInfo bufferInfo);
        void onStopped();
    }

    private String TAG = "AudioEncoder";
    private MediaCodec audioEncoder;
    private MediaCodec.BufferInfo audioInfo = new MediaCodec.BufferInfo();
    private boolean running;

    //default parameters for encoder
    private CodecUtil.Force force = CodecUtil.Force.FIRST_COMPATIBLE_FOUND;

    private Long mPresentTimeUs;
    private Callback mCallback;
    private MediaFormat mMediaFormat;


    public void setPresentTimeUs(long presentTimeUs) {
        mPresentTimeUs = presentTimeUs;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void setMediaFormat(MediaFormat mediaFormat) {
        mMediaFormat = mediaFormat;
    }

    /**
     * Prepare encoder with custom parameters
     */
    private boolean prepareAudioEncoder() {

        try {
            List<MediaCodecInfo> encoders = new ArrayList<>();
            if (force == CodecUtil.Force.HARDWARE) {
                encoders = CodecUtil.getAllHardwareEncoders(CodecUtil.AAC_MIME);
            } else if (force == CodecUtil.Force.SOFTWARE) {
                encoders = CodecUtil.getAllSoftwareEncoders(CodecUtil.AAC_MIME);
            }

            if (force == CodecUtil.Force.FIRST_COMPATIBLE_FOUND) {
                audioEncoder = MediaCodec.createEncoderByType(CodecUtil.AAC_MIME);
            } else {
                if (encoders.isEmpty()) {
                    Log.e(TAG, "Valid encoder not found");
                    return false;
                } else {
                    audioEncoder = MediaCodec.createByCodecName(encoders.get(0).getName());
                }
            }

            audioEncoder.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            running = false;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setForce(CodecUtil.Force force) {
        this.force = force;
    }

    public void start() throws Exception {

        if (mPresentTimeUs == null) {
            throw new IllegalArgumentException("mPresentTimeUs is null.");
        }
        if (mCallback == null) {
            throw new IllegalArgumentException("mCallback is null.");
        }
        if (mMediaFormat == null) {
            throw new IllegalArgumentException("mMediaFormat is null.");
        }

        // MediaFormatに必要な項目をチェック
        try {
            int samplerate = mMediaFormat.getInteger(KEY_SAMPLE_RATE);
        }
        catch(Exception exception) {
            throw new IllegalArgumentException("mMediaFormat(samplerate) is not found.");
        }
        try {
            int bitrate = mMediaFormat.getInteger(KEY_BIT_RATE);
        }
        catch(Exception exception) {
            throw new IllegalArgumentException("mMediaFormat(bitrate) is not found.");
        }
        try {
            int channelCount = mMediaFormat.getInteger(KEY_CHANNEL_COUNT);
        }
        catch(Exception exception) {
            throw new IllegalArgumentException("mMediaFormat(channelCount) is not found.");
        }

        prepareAudioEncoder();

        if (audioEncoder != null) {
            audioEncoder.start();
            running = true;
            Log.i(TAG, "AudioEncoder started");
        } else {
            Log.e(TAG, "AudioEncoder need be prepared, AudioEncoder not enabled");
        }
    }

    public void stop() {
        running = false;
        if (audioEncoder != null) {
            audioEncoder.stop();
            audioEncoder.release();
            audioEncoder = null;
        }
        mCallback.onStopped();
        Log.i(TAG, "AudioEncoder stopped");
    }

    /**
     * Set custom PCM data.
     * Use it after prepareAudioEncoder(int sampleRate, int channel).
     * Used too with microphone.
     *
     * @param data PCM buffer
     * @param size Min PCM buffer size
     */
    public void inputPCMData(byte[] data, int size) {

        // 入力バッファにPCMデータを書き込む。
        int inBufferIndex = audioEncoder.dequeueInputBuffer(-1);
        if (inBufferIndex >= 0) {
            ByteBuffer bb = audioEncoder.getInputBuffer(inBufferIndex);
            bb.put(data, 0, size);
            long pts = System.nanoTime() / 1000 - mPresentTimeUs;
            audioEncoder.queueInputBuffer(inBufferIndex, 0, size, pts, 0);
        }

        // 出力バッファにデータが設定されたらAACデータを通知しバッファを消去。データが無くなれば処理完了。
        for (; ; ) {
            int outBufferIndex = audioEncoder.dequeueOutputBuffer(audioInfo, 0);
            if (outBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                mCallback.onAudioFormat(audioEncoder.getOutputFormat());
            } else if (outBufferIndex >= 0) {
                //This ByteBuffer is AAC
                ByteBuffer bb = audioEncoder.getOutputBuffer(outBufferIndex);
                mCallback.onReceiveAacData(bb, audioInfo);
                audioEncoder.releaseOutputBuffer(outBufferIndex, false);
            } else {
                break;
            }
        }
    }

    private void getDataFromEncoder(byte[] data, int size) {
        ByteBuffer[] inputBuffers = audioEncoder.getInputBuffers();
        ByteBuffer[] outputBuffers = audioEncoder.getOutputBuffers();

        int inBufferIndex = audioEncoder.dequeueInputBuffer(-1);
        if (inBufferIndex >= 0) {
            ByteBuffer bb = inputBuffers[inBufferIndex];
            bb.clear();
            bb.put(data, 0, size);
            long pts = System.nanoTime() / 1000 - mPresentTimeUs;
            audioEncoder.queueInputBuffer(inBufferIndex, 0, size, pts, 0);
        }

        for (; ; ) {
            int outBufferIndex = audioEncoder.dequeueOutputBuffer(audioInfo, 0);
            if (outBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                mCallback.onAudioFormat(audioEncoder.getOutputFormat());
            } else if (outBufferIndex >= 0) {
                //This ByteBuffer is AAC
                ByteBuffer bb = outputBuffers[outBufferIndex];
                mCallback.onReceiveAacData(bb, audioInfo);
                audioEncoder.releaseOutputBuffer(outBufferIndex, false);
            } else {
                break;
            }
        }
    }

    public boolean isRunning() {
        return running;
    }
}
