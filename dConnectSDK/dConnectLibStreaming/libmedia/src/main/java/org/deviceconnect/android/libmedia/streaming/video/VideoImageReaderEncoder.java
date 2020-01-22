package org.deviceconnect.android.libmedia.streaming.video;

import android.media.MediaCodecInfo;

import java.nio.ByteBuffer;

/**
 * Camera2 API から ImageReader でプレビューを取得して、エンコードするためのエンコーダ.
 */
public abstract class VideoImageReaderEncoder extends VideoEncoder {
    private static final long INPUT_TIMEOUT = 500000;

    @Override
    public int getColorFormat() {
        return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible;
    }

    @Override
    protected void startRecording() {
    }

    @Override
    protected void stopRecording() {
    }

    void writeInputBuffer(byte[] bytes, long timestamp) throws IllegalStateException {
        ByteBuffer inputBuffer = null;

        int index = mMediaCodec.dequeueInputBuffer(INPUT_TIMEOUT);
        if (index >= 0) {
            inputBuffer = mMediaCodec.getInputBuffer(index);
        }

        if (inputBuffer == null) {
            return;
        }

        int size = bytes.length;
        inputBuffer.clear();
        inputBuffer.put(bytes, 0, size);
        mMediaCodec.queueInputBuffer(index, 0, size, timestamp, 0);
    }
}
