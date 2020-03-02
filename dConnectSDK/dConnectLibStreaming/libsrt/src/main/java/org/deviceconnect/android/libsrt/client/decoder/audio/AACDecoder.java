package org.deviceconnect.android.libsrt.client.decoder.audio;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libsrt.client.Frame;
import org.deviceconnect.android.libsrt.client.FrameCache;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

public class AACDecoder extends AudioDecoder {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "AACLATM-DECODE";

    /**
     * AAC で使用できるサンプリングレートを定義します.
     */
    private static final int[] SUPPORT_AUDIO_SAMPLING_RATES = {
            96000, // 0
            88200, // 1
            64000, // 2
            48000, // 3
            44100, // 4
            32000, // 5
            24000, // 6
            22050, // 7
            16000, // 8
            12000, // 9
            11025, // 10
            8000,  // 11
            7350,  // 12
            -1,   // 13
            -1,   // 14
            -1,   // 15
    };

    /**
     * デコードを行うMediaCodec.
     */
    private MediaCodec mMediaCodec;

    /**
     * 音声再生処理を行うスレッド.
     */
    private WorkThread mWorkThread;

    /**
     * フレームをキャッシュを管理するクラス.
     */
    private FrameCache mFrameCache;

    @Override
    public void onInit() {
        mFrameCache = new FrameCache();
        mFrameCache.initFrames();
    }

    @Override
    public void onReceived(byte[] data, int dataLength, long pts) {
        if (isRunningWorkThread()) {
            Frame frame = mFrameCache.getFrame(data, dataLength, (pts / 90000) * 1000 * 1000);
            if (frame == null) {
                if (DEBUG) {
                    Log.e(TAG, "No free frame.");
                }
                return;
            }
            mWorkThread.add(frame);
        } else {
            if ((data[0] & 0xFF) == 0xFF && (data[1] & 0xF0) == 0xF0) {
                int profile = ((data[2] & 0xFF) >> 6);
                int freqIdx = (((data[2] & 0xFF) >> 2) & 0x0F);
                setChannelCount(1);
                setSamplingRate(SUPPORT_AUDIO_SAMPLING_RATES[freqIdx]);

                mWorkThread = new WorkThread();
                mWorkThread.setName("AACLATM-DECODE");
                mWorkThread.start();
            }
        }
    }

    @Override
    public void onReleased() {
        if (mMediaCodec != null) {
            try {
                mMediaCodec.stop();
            } catch (Exception e) {
                // ignore.
            }

            try {
                mMediaCodec.release();
            } catch (Exception e) {
                // ignore.
            }

            mMediaCodec = null;
        }

        if (mWorkThread != null) {
            mWorkThread.close();
            mWorkThread = null;
        }

        releaseAudioTrack();
    }

    private void createMediaCodec() throws IOException {
        if (mMediaCodec != null) {
            if (DEBUG) {
                Log.w(TAG, "MediaCodec is already running.");
            }
            return;
        }

        MediaFormat format = MediaFormat.createAudioFormat("audio/mp4a-latm",
                getSamplingRate(), getChannelCount());

        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);

        mMediaCodec = MediaCodec.createDecoderByType("audio/mp4a-latm");
        mMediaCodec.configure(format, null, null, 0);
        mMediaCodec.start();
    }

    /**
     * WorkThread が動作しているか確認します.
     *
     * @return WorkThread が動作している場合はtrue、それ以外はfalse.
     */
    private boolean isRunningWorkThread() {
        return mWorkThread != null && mWorkThread.isAlive();
    }

    /**
     * 送られてきたデータをMediaCodecに渡してデコードを行うスレッド.
     */
    private class WorkThread extends Thread {
        /**
         * 送られてきたデータを格納するリスト.
         */
        private final Queue<Frame> mFrames = new LinkedList<>();

        /**
         * データの終了フラグ.
         */
        private boolean isEOS = false;

        /**
         * スレッドのクローズ処理を行います.
         */
        void close() {
            interrupt();

            try {
                join(500);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        /**
         * 送られてきたデータを通知します.
         *
         * @param frame フレームバッファ
         */
        synchronized void add(final Frame frame) {
            mFrames.offer(frame);
            notifyAll();
        }

        /**
         * フレームの先頭を取得すると同時に削除します.
         *
         * @return フレーム
         */
        private synchronized Frame getFrame() throws InterruptedException {
            while (mFrames.peek() == null) {
                wait();
            }
            return mFrames.remove();
        }

        @Override
        public void run() {
            try {
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

                createAudioTrack();

                createMediaCodec();

                while (!isInterrupted()) {
                    Frame frame = getFrame();

                    if (!isEOS) {
                        int inIndex = mMediaCodec.dequeueInputBuffer(10000);
                        if (inIndex >= 0) {
                            ByteBuffer buffer = mMediaCodec.getInputBuffer(inIndex);
                            if (buffer == null) {
                                continue;
                            }

                            buffer.clear();
                            buffer.put(frame.getBuffer(), 0, frame.getLength());
                            buffer.flip();

                            mMediaCodec.queueInputBuffer(inIndex, 0, frame.getLength(), frame.getPTS(), 0);
                        }
                    }

                    int outIndex = mMediaCodec.dequeueOutputBuffer(info, 10000);
                    if (outIndex > 0) {
                        if (info.size > 0) {
                            writeAudioData(mMediaCodec.getOutputBuffer(outIndex), 0, info.size, info.presentationTimeUs);
                        }
                        mMediaCodec.releaseOutputBuffer(outIndex, true);
                    } else {
                        switch (outIndex) {
                            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                                if (DEBUG) {
                                    Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                                }
                                break;

                            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                                if (DEBUG) {
                                    Log.d(TAG, "INFO_OUTPUT_FORMAT_CHANGED");
                                    Log.d(TAG, "New format " + mMediaCodec.getOutputFormat());
                                }
                                break;

                            case MediaCodec.INFO_TRY_AGAIN_LATER:
                                Thread.sleep(1);
                                break;

                            default:
                                break;
                        }
                    }

                    // All decoded frames have been rendered, we can stop playing now
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if (DEBUG) {
                            Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                        }
                        break;
                    }
                }
            } catch (InterruptedException e) {
                // ignore.
            } catch (Exception e) {
                if (DEBUG) {
                    Log.w(TAG, "AAC encode occurred an exception.", e);
                }
            }
        }
    }
}
