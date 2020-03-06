package org.deviceconnect.android.libsrt.client.decoder.audio;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.util.QueueThread;
import org.deviceconnect.android.libsrt.client.Frame;
import org.deviceconnect.android.libsrt.client.FrameCache;

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

    /**
     * サンプルレートのインデックス.
     */
    private int mFreqIdx;

    @Override
    public void onInit() {
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

                mFreqIdx = freqIdx;
                setChannelCount(channel);
                setSamplingRate(SUPPORT_AUDIO_SAMPLING_RATES[freqIdx]);

                if (mFrameCache != null) {
                    mFrameCache.freeFrames();
                }
                mFrameCache = new FrameCache();
                mFrameCache.initFrames();

                mWorkThread = new WorkThread();
                mWorkThread.setName("AACLATM-DECODER");
                mWorkThread.start();
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

        int audioProfile = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
        int sampleIndex = mFreqIdx;
        int channelConfig = getChannelCount();

        ByteBuffer csd = ByteBuffer.allocate(2);
        csd.put((byte) ((audioProfile << 3) | (sampleIndex >> 1)));
        csd.position(1);
        csd.put((byte) ((byte) ((sampleIndex << 7) & 0x80) | (channelConfig << 3)));
        csd.flip();
        format.setByteBuffer("csd-0", csd);

        if (DEBUG) {
            Log.d(TAG, "MediaFormat: " + format);
        }

        if (mMediaCodec != null) {
            try {
                mMediaCodec.stop();
                mMediaCodec.release();
            } catch (Exception e) {
                // ignore.
            }
        }

        mMediaCodec = MediaCodec.createDecoderByType("audio/mp4a-latm");
        mMediaCodec.configure(format, null, null, 0);
        mMediaCodec.start();
    }

    private void releaseMediaCodec() {
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
         * スレッドのクローズ処理を行います.
         */
        void terminate() {
            interrupt();

            try {
                join(500);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        @Override
        public void run() {
            try {
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

                createAudioTrack();
                createMediaCodec();

                while (!isInterrupted()) {
                    Frame frame = get();

                    int inIndex = mMediaCodec.dequeueInputBuffer(TIMEOUT_US);
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

                    frame.release();

                    int outIndex = mMediaCodec.dequeueOutputBuffer(info, TIMEOUT_US);
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
            } finally {
                releaseAudioTrack();
                releaseMediaCodec();
            }
        }
    }
}
