package org.deviceconnect.android.librtmpstreaming;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoEncoderCore {
    private static final String TAG = "VideoEncoderCore";
    private static final boolean DEBUG = false;

    private MediaFormat mMediaFormat;
    private VideoEncodeCallback callback;

    private Surface mInputSurface;

    private MediaCodec mEncoder;
    private MediaCodec.BufferInfo mBufferInfo;

    private Long mPresentTimeUs;

    private Thread mVideoEncoderThread;

    /**
     * mVideoEncoderThread実行状態.
     */
    private enum VideoEncoderThreadStatus {
        /**
         * スレッド停止状態.
         */
        STOP,
        /**
         * スレッド停止命令を受けて、最後にdrainEncoder(true)を実行しSTOPに移行するまでの状態.
         */
        STOPPING,
        /**
         * スレッド実行開始直後からstop()を実行されるまでの状態.
         */
        RUNNING,
    }
    private VideoEncoderThreadStatus mVideoEncoderThreadStatus = VideoEncoderThreadStatus.STOP;

    public VideoEncoderCore() {
        mBufferInfo = new MediaCodec.BufferInfo();
    }

    public void setMediaFormat(MediaFormat mediaFormat) {
        mMediaFormat = mediaFormat;
    }

    public void setCallback(VideoEncodeCallback callback) {
        this.callback = callback;
    }

    public void setPresentTimeUs(long presentTimeUs) {
        // mPresentTimeUs = System.nanoTime() / 1000;
        mPresentTimeUs = presentTimeUs;
    }


    /**
     * Returns the encoder's input surface.
     */
    public Surface getInputSurface() {
        return mInputSurface;
    }

    /**
     * MediaCodecを作成して処理開始する.
     * @throws IllegalStateException 事前に設定する値が設定されていない.
     * @throws IOException MediaCodecが作成できなかった場合等.
     */
    public void start() throws IllegalStateException, IOException {
        if (DEBUG) Log.d(TAG, "start encoder");
        if (mMediaFormat == null) {
            throw new IllegalStateException("mMediaFormat is null.");
        }
        if (callback == null) {
            throw new IllegalStateException("mCallback is null.");
        }
        if (mPresentTimeUs  == null) {
            throw new IllegalStateException("mPresentTimeUs is null.");
        }

        // Create a MediaCodec encoder, and configure it with our format.  Get a Surface
        // we can use for input and wrap it with a class that handles the EGL work.
        String mimeType = mMediaFormat.getString(MediaFormat.KEY_MIME);
        mEncoder = MediaCodec.createEncoderByType(mimeType);
        mEncoder.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = mEncoder.createInputSurface();
        mEncoder.start();
        startVideoEncoderThread();
    }

    /**
     * MediaCodecを停止する.
     */
    public void stop() {
        if (DEBUG) Log.d(TAG, "stop encoder");
        stopVideoEncoderThread();
        // stop処理完了後にstopped()を実行する.

    }
    private void stopped() {
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
    }

    /**
     * VideoEncoderスレッドを開始.
     */
    private void startVideoEncoderThread() {

        mVideoEncoderThread = new Thread(new Runnable() {
            @Override
            public void run() {

                synchronized (mVideoEncoderThreadStatus) {
                    mVideoEncoderThreadStatus = VideoEncoderThreadStatus.RUNNING;
                }

                try {
                    while (true) {
                        // 停止命令を受けたらwhileループを抜けてdrainEncoder(true)を実行しスレッド停止する.
                        synchronized (mVideoEncoderThreadStatus) {
                            if (mVideoEncoderThreadStatus != VideoEncoderThreadStatus.RUNNING) {
                                break;
                            }
                        }
                        drainEncoder(mVideoEncoderThread, false);
                    }
                    drainEncoder(mVideoEncoderThread, true);

                    // stop処理完了後に行う処理
                    stopped();
                    callback.onStopped();
                }
                catch (Exception exception) {
                    callback.onFailure("Video encoder thread error." + exception.getMessage());
                }
                finally {
                    // スレッド停止
                    synchronized (mVideoEncoderThreadStatus) {
                        mVideoEncoderThreadStatus = VideoEncoderThreadStatus.STOPPING;
                    }
                    mVideoEncoderThread.interrupt();
                    mVideoEncoderThread = null;
                }
            }
        });
        mVideoEncoderThread.start();
    }

    /**
     * VideoEncoderスレッドを停止.
     */
    private void stopVideoEncoderThread() {
        synchronized (mVideoEncoderThreadStatus) {
            if (mVideoEncoderThreadStatus == VideoEncoderThreadStatus.RUNNING) {
                mVideoEncoderThreadStatus = VideoEncoderThreadStatus.STOPPING;
            }
        }
    }


    /**
     * Extracts all pending data from the encoder and forwards it to the muxer.
     * <p>
     * If endOfStream is not set, this returns when there is no more data to drain.  If it
     * is set, we send EOS to the encoder, and then iterate until we see EOS on the output.
     * Calling this with endOfStream set should be done once, right before stopping the muxer.
     * <p>
     * We're just using the muxer to get a .mp4 file (instead of a raw H.264 stream).  We're
     * not recording audio.
     *
     * 保留中のすべてのデータをエンコーダから抽出し、それをマルチプレクサに転送します。
     *
     * endOfStreamが設定されていない場合、排除するデータがなくなったときに戻ります。
     * 設定されている場合は、EOSをエンコーダに送り、出力上でEOSが見えるまで繰り返します。
     *
     * endOfStream setでこれを呼び出すのは、muxerを停止する直前に一度実行する必要があります。
     *
     * muxerを使って、（raw H.264ストリームではなく）.mp4ファイルを取得しています。
     * 私たちはオーディオを録音していません。
     *
     * @throws Exception MediaCodecの処理中に例外が発生した場合.
     *
     */
    private void drainEncoder(Thread videoEncoderThread, boolean endOfStream) throws Exception {

        final int TIMEOUT_USEC = 10000;
        if (DEBUG) Log.d(TAG, "drainEncoder(" + endOfStream + ")");

        if (endOfStream) {
            if (DEBUG) Log.d(TAG, "sending EOS to encoder");
            mEncoder.signalEndOfInputStream();
        }

        ByteBuffer[] encoderOutputBuffers = mEncoder.getOutputBuffers();
        while (!videoEncoderThread.isInterrupted()) {
            int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    break;      // out of while
                } else {
                    if (DEBUG) Log.d(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                MediaFormat newFormat = mEncoder.getOutputFormat();
                if (DEBUG) Log.d(TAG, "encoder output format changed: " + newFormat);

                callback.onOutputFormatChanged(newFormat);

            } else if (encoderStatus < 0) {
                if (DEBUG) Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
                // let's ignore it
            } else {
                mBufferInfo.presentationTimeUs = System.nanoTime() / 1000 - mPresentTimeUs;
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    if (DEBUG) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                }

                if (mBufferInfo.size != 0) {

                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);

                    if (this.callback != null) {
                        this.callback.onReceiveSampleData(encodedData, mBufferInfo);
                    }
                    if (DEBUG) {
                        Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer, ts=" +
                                mBufferInfo.presentationTimeUs);
                    }
                }

                mEncoder.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        if (DEBUG) Log.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        if (DEBUG) Log.d(TAG, "end of stream reached");
                    }
                    break;      // out of while
                }
            }
        }
    }

    public interface VideoEncodeCallback {
        void onReceiveSampleData(ByteBuffer encodedData, MediaCodec.BufferInfo mBufferInfo);
        void onOutputFormatChanged(MediaFormat newFormat);
        void onFailure(String errorMessage);
        void onStopped();
    }
}
