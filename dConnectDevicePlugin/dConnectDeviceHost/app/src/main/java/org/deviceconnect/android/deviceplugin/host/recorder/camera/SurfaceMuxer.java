/*
 SurfaceMuxer.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;

import java.io.IOException;
import java.nio.ByteBuffer;

class SurfaceMuxer {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "host.dplugin";

    private static final String MIME_TYPE = "video/avc";
    private static final int DEFAULT_BIT_RATE = 1 * 1000 * 1000;
    private static final int DEFAULT_FRAME_RATE = 30;
    private static final int I_FRAME_INTERVAL = 1;

    private final MediaCodec mCodec;
    private final MediaMuxer mMuxer;
    private final Surface mInputSurface;

    private boolean mMuxerStarted;
    private boolean mEndOfStream;
    private int mTrackIndex = -1;

    SurfaceMuxer(final String outputFile, final int width, final int height) throws IOException {
        mMuxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, DEFAULT_BIT_RATE);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, DEFAULT_FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL);
        
        mCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        mCodec.setCallback(new MediaCodec.Callback() {

            int fps = 30;
            long start = 0L;
            long now = 0L;
            double base = 0.0;

            @Override
            public void onInputBufferAvailable(final @NonNull MediaCodec codec, final int index) {
                // NOP.
                if (DEBUG) {
                    Log.d(TAG, "onInputBufferAvailable:");
                }
            }

            @Override
            public void onOutputFormatChanged(final @NonNull MediaCodec codec, final @NonNull MediaFormat format) {
                if (DEBUG) {
                    Log.d(TAG, "onOutputFormatChanged:");
                }
                if (mMuxerStarted) {
                    throw new RuntimeException("Mux has started already.");
                }
                mTrackIndex = mMuxer.addTrack(format);
                mMuxer.start();
                mMuxerStarted = true;
            }

            @Override
            public void onOutputBufferAvailable(final @NonNull MediaCodec codec,
                                                final int index,
                                                final @NonNull MediaCodec.BufferInfo info) {
                if (DEBUG) {
                    Log.d(TAG, "onOutputBufferAvailable:");
                }
                long time = System.currentTimeMillis();
                if (start == 0L) {
                    start = time;
                }
                now = time;

                ByteBuffer outputBuffer = codec.getOutputBuffer(index);
                if (outputBuffer == null) {
                    if (DEBUG) {
                        Log.e(TAG, "onOutputBufferAvailable: buffer is null");
                    }
                    return;
                }

                if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    info.size = 0;
                }
                if (info.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("Not started yet.");
                    }
                    outputBuffer.position(info.offset);
                    outputBuffer.limit(info.offset + info.size);
                    if (now - start >= base) {
                        base += 1000 / fps;
                        mMuxer.writeSampleData(mTrackIndex, outputBuffer, info);
                    }
                }
                codec.releaseOutputBuffer(index, false);

                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!mEndOfStream) {
                        if (DEBUG) {
                            Log.e(TAG, "Unexpected EOF.");
                        }
                    }
                }
            }

            @Override
            public void onError(final @NonNull MediaCodec codec, final @NonNull MediaCodec.CodecException e) {
                if (DEBUG) {
                    Log.w(TAG, "onError: Failed to encode video.", e);
                }
            }
        });
        mCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = mCodec.createInputSurface();
    }

    public Surface getInputSurface() {
        return mInputSurface;
    }

    public void start() {
        mCodec.start();
    }

    public void stop() {
        mEndOfStream = true;
        mCodec.signalEndOfInputStream();
    }

    public void release() {
        mCodec.stop();
        mCodec.release();
        mMuxer.stop();
        mMuxer.release();
    }
}
