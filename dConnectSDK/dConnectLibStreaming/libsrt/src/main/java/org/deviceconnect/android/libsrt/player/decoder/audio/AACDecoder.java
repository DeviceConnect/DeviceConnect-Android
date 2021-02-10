package org.deviceconnect.android.libsrt.player.decoder.audio;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.util.QueueThread;
import org.deviceconnect.android.libsrt.player.Frame;
import org.deviceconnect.android.libsrt.player.FrameCache;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AACDecoder extends AudioDecoder {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "AACDecoder";

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
     * 音声再生処理を行うスレッド.
     */
    private WorkThread mWorkThread;

    /**
     * フレームをキャッシュを管理するクラス.
     */
    private FrameCache mFrameCache;

    @Override
    public void onInit() {
        if (mFrameCache != null) {
            mFrameCache.freeFrames();
        }
        mFrameCache = new FrameCache();
        mFrameCache.initFrames();
    }

    @Override
    public void onReceived(byte[] data, int dataLength, long pts) {
        if (isRunningWorkThread()) {
            pts = (long)((pts / (float) 90000) * 1000 * 1000);

            Frame frame;
            if (hasCRC(data)) {
                frame = mFrameCache.getFrame(data, 9, dataLength - 9, pts);
            } else {
                frame = mFrameCache.getFrame(data, 7, dataLength - 7, pts);
            }

            if (frame == null) {
                if (DEBUG) {
                    Log.e(TAG, "No free frame.");
                }
                return;
            }
            mWorkThread.add(frame);
        } else {
            if (isADTS(data)) {
                boolean crc = (data[1] & 0x01) != 0;
                int profile = ((data[2] & 0xFF) >> 6);
                int freqIdx = (((data[2] & 0xFF) >> 2) & 0x0F);
                int channel = ((data[2] & 0x01) << 2) | ((data[3] & 0xFF) >> 6);
                int frameLength = ((data[3] & 0x03) << 11) | ((data[4] & 0xFF) << 3) | ((data[5] & 0xFF) >> 5);

                if (DEBUG) {
                    Log.d(TAG, "ADTS");
                    Log.d(TAG, "   crc: " + crc);
                    Log.d(TAG, "   profile: " + profile);
                    Log.d(TAG, "   freqIdx: " + freqIdx + " " + SUPPORT_AUDIO_SAMPLING_RATES[freqIdx]);
                    Log.d(TAG, "   channel: " + channel);
                    Log.d(TAG, "   frameLength: " + frameLength);
                    Log.d(TAG, "   dataLength: " + dataLength);
                }

                setChannelCount(channel);
                setSamplingRate(SUPPORT_AUDIO_SAMPLING_RATES[freqIdx]);

                createWorkThread();

                mFrameCache.releaseAllFrames();
            }
        }
    }

    @Override
    public void onReleased() {
        if (mWorkThread != null) {
            mWorkThread.terminate();
            mWorkThread = null;
        }

        if (mFrameCache != null) {
            mFrameCache.freeFrames();
            mFrameCache = null;
        }
    }

    /**
     * ADTS ヘッダーの sync word を確認します.
     *
     * @param data データ
     * @return ADTS ヘッダーの場合はtrue、それ以外はfalse
     */
    private boolean isADTS(byte[] data) {
        return (data[0] & 0xFF) == 0xFF && (data[1] & 0xF0) == 0xF0;
    }

    /**
     * ADTS ヘッダーの CRC Protection が有効になっているか確認します.
     *
     * @param data データ
     * @return CRC Protection が有効の場合はtrue、それ以外は false
     */
    private boolean hasCRC(byte[] data) {
        return (data[1] & 0x01) == 0;
    }

    /**
     * ADTS に指定するサンプルレートに対応するインデックスを取得します.
     *
     * @param sampleRate サンプルレート
     * @return サンプルレートに対応するインデックス
     */
    private int getFreqIdx(int sampleRate) {
        for (int i = 0; i < SUPPORT_AUDIO_SAMPLING_RATES.length; i++) {
            if (sampleRate == SUPPORT_AUDIO_SAMPLING_RATES[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * MediaCodec を作成します.
     *
     * @return MediaCodec
     * @throws IOException MediaCodec の作成に失敗した場合に発生
     */
    private MediaCodec createMediaCodec() throws IOException {
        MediaFormat format = MediaFormat.createAudioFormat("audio/mp4a-latm",
                getSamplingRate(), getChannelCount());

        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);

        int audioProfile = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
        int sampleIndex = getFreqIdx(getSamplingRate());
        int channelConfig = getChannelCount();

        ByteBuffer csd = ByteBuffer.allocateDirect(2);
        csd.put((byte) ((audioProfile << 3) | (sampleIndex >> 1)));
        csd.position(1);
        csd.put((byte) ((byte) ((sampleIndex << 7) & 0x80) | (channelConfig << 3)));
        csd.flip();

        format.setByteBuffer("csd-0", csd);

        if (DEBUG) {
            Log.d(TAG, "AACDecoder::createMediaCodec: " + format);
        }

        MediaCodec mediaCodec = MediaCodec.createDecoderByType("audio/mp4a-latm");
        mediaCodec.configure(format, null, null, 0);
        mediaCodec.start();
        return mediaCodec;
    }

    /**
     * Surface に描画を行うスレッドを作成します.
     */
    private void createWorkThread() {
        if (mWorkThread != null) {
            mWorkThread.terminate();
        }

        mWorkThread = new WorkThread();
        mWorkThread.setName("AACLATM-DECODER");
        mWorkThread.start();
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
    private class WorkThread extends QueueThread<Frame> {
        /**
         * タイムアウト時間を定義.
         */
        private static final long TIMEOUT_US = 50000;

        /**
         * デコードを行うMediaCodec.
         */
        private MediaCodec mMediaCodec;

        /**
         * 停止フラグ.
         */
        private boolean mStopFlag;

        /**
         * スレッドのクローズ処理を行います.
         */
        void terminate() {
            mStopFlag = true;

            interrupt();

            releaseMediaCodec();

            try {
                join(500);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        private synchronized void releaseMediaCodec() {
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
        }

        @Override
        public void run() {
            try {
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

                createAudioTrack();

                mMediaCodec = createMediaCodec();

                while (!mStopFlag) {
                    Frame frame = get();

                    int inIndex = mMediaCodec.dequeueInputBuffer(TIMEOUT_US);
                    if (inIndex >= 0 && !mStopFlag) {
                        ByteBuffer buffer = mMediaCodec.getInputBuffer(inIndex);
                        if (buffer != null) {
                            buffer.clear();
                            buffer.put(frame.getBuffer(), 0, frame.getLength());
                            buffer.flip();
                        }

                        mMediaCodec.queueInputBuffer(inIndex, 0, frame.getLength(), frame.getPTS(), 0);
                    }

                    frame.release();

                    int outIndex = mMediaCodec.dequeueOutputBuffer(info, TIMEOUT_US);
                    if (outIndex >= 0 && !mStopFlag) {
                        if (info.size > 0) {
                            writeAudioData(mMediaCodec.getOutputBuffer(outIndex), 0, info.size, info.presentationTimeUs);
                        }
                        mMediaCodec.releaseOutputBuffer(outIndex, false);
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
                }
            } catch (OutOfMemoryError e) {
                if (DEBUG) {
                    Log.w(TAG, "Out of memory.", e);
                }
            } catch (InterruptedException e) {
                // ignore.
            } catch (Exception e) {
                if (DEBUG) {
                    Log.w(TAG, "AAC encode occurred an exception.", e);
                }
                if (!mStopFlag) {
                    postError(e);
                }
            } finally {
                releaseAudioTrack();
                releaseMediaCodec();
            }
        }
    }
}
