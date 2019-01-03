/*
 SurfaceRecorder.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

    private static final String VIDEO_MIME_TYPE = "video/avc";
    private static final int VIDEO_BIT_RATE = 1 * 1000 * 1000;
    private static final int VIDEO_FRAME_RATE = 30;
    private static final int VIDEO_I_FRAME_INTERVAL = 1;

    private static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";
    private static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int AUDIO_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_CHANNEL_COUNT = 1;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int AUDIO_BIT_RATE = 128 * 1000; // 128Kbps
    private static final int AUDIO_BUFFER_SIZE = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_CHANNEL_CONFIG, AUDIO_FORMAT);

    private MediaCodec mVideoEncoder;
    private AudioRecord mAudioRecord;
    private MediaCodec mAudioEncoder;
    private MediaMuxer mMuxer;
    private File mOutputFile;
    private Surface mInputSurface;

    private boolean mMuxerStarted;
    private boolean mEndOfStream;
    private Thread mMuxerThread;
    private Thread mVideoOutputThread;
    private Thread mAudioOutputThread;
    private Thread mAudioInputThread;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    SurfaceRecorder(final Size size) throws IOException {
        int width = size.getWidth();
        int height = size.getHeight();
        if (DEBUG) {
            Log.d(TAG, "SurfaceRecorder: width=" + width + " height=" + height);
        }

        mVideoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE);
        mVideoEncoder.configure(createVideoFormat(width, height), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = mVideoEncoder.createInputSurface();

        mAudioEncoder = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE);
        mAudioEncoder.configure(createAudioFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mAudioRecord = new AudioRecord(AUDIO_SOURCE, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL_CONFIG, AUDIO_FORMAT, AUDIO_BUFFER_SIZE);
    }

    private MediaFormat createVideoFormat(int width, int height) {
        MediaFormat format = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, VIDEO_BIT_RATE);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, VIDEO_FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VIDEO_I_FRAME_INTERVAL);
        return format;
    }

    private MediaFormat createAudioFormat() {
        MediaFormat format = MediaFormat.createAudioFormat(AUDIO_MIME_TYPE, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL_COUNT);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
        format.setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BIT_RATE);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
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
        if (mVideoEncoder == null) {
            throw new IllegalStateException("codec has been released.");
        }

        mMuxerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mVideoEncoder.start();
                    mAudioEncoder.start();

                    Future<MediaFormat> videoFormat = mExecutor.submit(new MediaFormatTask(mVideoEncoder));
                    Future<MediaFormat> audioFormat = mExecutor.submit(new MediaFormatTask(mAudioEncoder));
                    int videoTrack = mMuxer.addTrack(videoFormat.get());
                    int audioTrack = mMuxer.addTrack(audioFormat.get());
                    mMuxer.start();
                    mMuxerStarted = true;

                    if (mVideoOutputThread == null) {
                        mVideoOutputThread = new Thread(new MediaOutputTask(mVideoEncoder, videoTrack));
                        mVideoOutputThread.start();
                    }
                    if (mAudioOutputThread == null) {
                        mAudioOutputThread = new Thread(new MediaOutputTask(mAudioEncoder, audioTrack));
                        mAudioOutputThread.start();
                    }
                    if (mAudioInputThread == null) {
                        mAudioInputThread = new Thread(new AudioInputTask());
                        mAudioInputThread.start();
                    }
                } catch (InterruptedException e) {
                    // NOP.
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    // NOP.
                    e.printStackTrace();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        mMuxerThread.start();
    }

    public synchronized File stop() {
        if (mVideoEncoder == null) {
            throw new IllegalStateException("codec has been released.");
        }

        mEndOfStream = true;
        mVideoEncoder.signalEndOfInputStream();

        try {
            if (mAudioInputThread != null) {
                mAudioInputThread.interrupt();
                mAudioInputThread.join();
            }
            if (mAudioOutputThread != null) {
                mAudioOutputThread.interrupt();
                mAudioOutputThread.join();
            }
            if (mVideoOutputThread != null) {
                mVideoOutputThread.interrupt();
                mVideoOutputThread.join();
            }
        } catch (InterruptedException e) {
            // NOP
        } finally {
            mAudioInputThread = null;
            mAudioOutputThread = null;
            mVideoOutputThread = null;
            if (mMuxer != null) {
                mMuxer.stop();
                mMuxer.release();
                mMuxer = null;
                mMuxerStarted = false;
            }
        }

        return mOutputFile;
    }

    public synchronized void release() {
        if (mVideoEncoder != null) {
            mVideoEncoder.stop();
            mVideoEncoder.release();
        }
        if (mAudioEncoder != null) {
            mAudioEncoder.stop();
            mAudioEncoder.release();
        }
        mInputSurface = null;
    }

    private String generateVideoFileName() {
        return "video_" + DATE_FORMAT.format(new Date()) + ".mp4";
    }

    public File getOutputFile() {
        return mOutputFile;
    }

    /**
     * コーデック固有の {@link MediaFormat} を取得するタスク.
     *
     * 取得した {@link MediaFormat} は {@link MediaMuxer#addTrack(MediaFormat)} の引数として使用する.
     */
    private static class MediaFormatTask implements Callable<MediaFormat> {

        final MediaCodec mMediaCodec;

        MediaFormatTask(final MediaCodec mediaCodec) {
            mMediaCodec = mediaCodec;
        }

        @Override
        public MediaFormat call() throws Exception {
            final MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            final int timeoutUs = 1000 * 1000;

            while (!Thread.interrupted()) {
                int index = mMediaCodec.dequeueOutputBuffer(info, timeoutUs);
                if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    break;
                }
            }
            return mMediaCodec.getOutputFormat();
        }
    }

    private class MediaOutputTask implements Runnable {

        private final MediaCodec mMediaCodec;
        private final int mTrackIndex;

        MediaOutputTask(final MediaCodec mediaCodec, final int trackIndex) {
            mMediaCodec = mediaCodec;
            mTrackIndex = trackIndex;
        }

        private long prevOutputPTSUs = 0;

        protected long getPTSUs() {
            long result = System.nanoTime() / 1000L;
            // presentationTimeUs should be monotonic
            // otherwise muxer fail to write
            if (result < prevOutputPTSUs) {
                result = (prevOutputPTSUs - result) + result;
            }
            prevOutputPTSUs = result;
            return result;
        }

        @Override
        public void run() {
            final MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            final int timeoutUs = 1000 * 1000;

            try {
                while (!Thread.interrupted()) {
                    int index = mMediaCodec.dequeueOutputBuffer(info, timeoutUs);
                    if (index >= 0) {

                        ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(index);
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
                            info.presentationTimeUs = getPTSUs();
                            mMuxer.writeSampleData(mTrackIndex, outputBuffer, info);
                        }
                        mMediaCodec.releaseOutputBuffer(index, false);

                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            if (!mEndOfStream) {
                                if (DEBUG) {
                                    Log.e(TAG, "Unexpected EOF.");
                                }
                            }
                        }
                    }
                }
                mMediaCodec.stop();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error on output thread: " + mMediaCodec.getName(), e);
            }

        }
    }

    private class AudioInputTask implements Runnable {

        private long prevOutputPTSUs = 0;

        protected long getPTSUs() {
            long result = System.nanoTime() / 1000L;
            // presentationTimeUs should be monotonic
            // otherwise muxer fail to write
            if (result < prevOutputPTSUs) {
                result = (prevOutputPTSUs - result) + result;
            }
            prevOutputPTSUs = result;
            return result;
        }

        @Override
        public void run() {
            debug("Start audio encoding.");
            mAudioRecord.startRecording();

            final int bufferSize = 1024;
            int len;
            byte[] data = new byte[bufferSize];
            final long timeoutUs = 1000 * 1000;

            try {
                while (mMuxerStarted) {
                    boolean interrupted = Thread.interrupted();

                    if ((len = mAudioRecord.read(data, 0, data.length)) > 0) {
                        debug("Read data: len=" + len);

                        int index = mAudioEncoder.dequeueInputBuffer(timeoutUs);
                        if (index != -1) {
                            ByteBuffer inputBuffer = mAudioEncoder.getInputBuffer(index);
                            inputBuffer.put(data, 0, len);

                            int flags = interrupted ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0;
                            mAudioEncoder.queueInputBuffer(index, 0, len, getPTSUs(), flags);
                        }
                    }
                    if (interrupted) {
                        break;
                    }
                }
            } catch (IllegalStateException e) {
                error("Failed to input data to encoder.", e);
            } finally {
                mAudioRecord.stop();
                debug("Stopped audio encoding.");
            }
        }
    }

    private static void debug(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }

    private static void error(String message, Throwable error) {
        Log.e(TAG, message, error);
    }
}
