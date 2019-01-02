/*
 SurfaceRecorder.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class SurfaceRecorder {

    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * ログ出力用タグ.
     */
    private static final String TAG = "host.dplugin";

    /**
     * 日付のフォーマット.
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.JAPAN);

    private static final String MIME_TYPE = "video/avc";
    private static final int DEFAULT_BIT_RATE = 1 * 1000 * 1000;
    private static final int DEFAULT_FRAME_RATE = 30;
    private static final int I_FRAME_INTERVAL = 1;

    private MediaCodec mCodec;
    private MediaMuxer mMuxer;
    private File mOutputFile;
    private Surface mInputSurface;
    private boolean mMuxerStarted;
    private boolean mEndOfStream;
    private int mTrackIndex = -1;

    private Thread mVideoMuxerThread;

    SurfaceRecorder(final Size size) throws IOException {
        int width = size.getWidth();
        int height = size.getHeight();
        if (DEBUG) {
            Log.d(TAG, "SurfaceRecorder: width=" + width + " height=" + height);
        }

        mCodec = MediaCodec.createEncoderByType(MIME_TYPE);

        MediaFormat format = createMediaFormat(width, height);
        mCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = mCodec.createInputSurface();
    }

    private MediaFormat createMediaFormat(int width, int height) {
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, DEFAULT_BIT_RATE);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, DEFAULT_FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL);
        return format;
    }

    public void initMuxer(final File basePath) throws RecorderException {
        try {
            mOutputFile = new File(basePath, generateVideoFileName());
            mMuxer = new MediaMuxer(mOutputFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            throw new RecorderException(RecorderException.REASON_FATAL, e);
        }
    }

    public Surface getInputSurface() {
        return mInputSurface;
    }

    public synchronized void start() {
        if (mCodec == null) {
            throw new IllegalStateException("codec has been released.");
        }

        if (mVideoMuxerThread == null) {
            mVideoMuxerThread = new Thread(new VideoMuxerTask());
            mVideoMuxerThread.start();
        }
    }

    public synchronized File stop() {
        if (mCodec == null) {
            throw new IllegalStateException("codec has been released.");
        }

        mEndOfStream = true;
        mCodec.signalEndOfInputStream();

        if (mVideoMuxerThread != null) {
            mVideoMuxerThread.interrupt();
            mVideoMuxerThread = null;
        }

        if (mMuxer != null) {
            mMuxer.stop();
            mMuxer.release();
            mMuxer = null;
        }

        return mOutputFile;
    }

    public synchronized void release() {
        if (mCodec != null) {
            mCodec.stop();
            mCodec.release();
        }
        mInputSurface = null;
    }

    private String generateVideoFileName() {
        return "video_" + DATE_FORMAT.format(new Date()) + ".mp4";
    }

    public File getOutputFile() {
        return mOutputFile;
    }

    private class VideoMuxerTask implements Runnable {

        @Override
        public void run() {
            mCodec.start();

            final MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            final int timeoutUs = 1000 * 1000;

            int fps = 30;
            long start = 0L;
            long now = 0L;
            double base = 0.0;

            try {
                while (!Thread.interrupted()) {
                    int index = mCodec.dequeueOutputBuffer(info, timeoutUs);
                    if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        mTrackIndex = mMuxer.addTrack(mCodec.getOutputFormat());
                        mMuxer.start();
                        mMuxerStarted = true;
                    } else if (index >= 0) {
                        long time = System.currentTimeMillis();
                        if (start == 0L) {
                            start = time;
                        }
                        now = time;

                        ByteBuffer outputBuffer = mCodec.getOutputBuffer(index);
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
                        mCodec.releaseOutputBuffer(index, false);

                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            if (!mEndOfStream) {
                                if (DEBUG) {
                                    Log.e(TAG, "Unexpected EOF.");
                                }
                            }
                        }
                    }
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error on video thread.", e);
            } finally {
                mCodec.stop();
            }

        }
    }
}
