package org.deviceconnect.android.libsrt.client.decoder.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import org.deviceconnect.android.libmedia.streaming.util.H265Parser;
import org.deviceconnect.android.libmedia.streaming.util.QueueThread;
import org.deviceconnect.android.libsrt.BuildConfig;
import org.deviceconnect.android.libsrt.client.Frame;
import org.deviceconnect.android.libsrt.client.FrameCache;

import java.io.IOException;
import java.nio.ByteBuffer;

public class H265Decoder extends VideoDecoder {
    /**
     * デバッグ用フラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "SRT-PLAYER";

    /**
     * MediaCodec に渡すマイムタイプ.
     */
    private String mMimeType = "video/hevc";

    private byte[] mVPS;
    private byte[] mSPS;
    private byte[] mPPS;

    /**
     * MediaCodec に渡す横幅.
     */
    private int mWidth;

    /**
     * MediaCodec に渡す縦幅.
     */
    private int mHeight;

    /**
     * デコード処理を行うスレッド.
     */
    private WorkThread mWorkThread;

    /**
     * フレームをキャッシュを管理するクラス.
     */
    private FrameCache mFrameCache;

    @Override
    public void onInit() {

    }

    @Override
    public void onReceived(byte[] data, int dataLength, long pts) {
        pts = (long) ((pts / (float) 90000) * 1000 * 1000);

        if (isRunningWorkThread()) {
            Frame frame = mFrameCache.getFrame(data, dataLength, pts);
            if (frame == null) {
                if (DEBUG) {
                    Log.e(TAG, "No free frame.");
                }
                return;
            }
            mWorkThread.add(frame);
        } else if (searchVPS(data, dataLength)) {
            createWorkThread();

            // TODO VPS、SPS、PPS のデータはキャッシュしておく。

            mWorkThread.add(mFrameCache.getFrame(data, dataLength, pts));
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
     * VPS、SPS、 PPS のパケットを探してのデータを作成します.
     *
     * @param data フレームバッファ
     * @return VPS、SPS、PPSが作成できた場合はtrue、それ以外はfalse
     */
    private boolean searchVPS(byte[] data, int dataLength) {
        for (int i = 0; i + 5 < dataLength; i++) {
            if (data[i] == 0x00 && data[i + 1] == 0x00 && data[i + 2] == 0x00 && data[i + 3] == 0x01) {
                int end = i + 4;
                for (; end + 4 < dataLength; end++) {
                    if (data[end] == 0x00 && data[end + 1] == 0x00 && data[end + 2] == 0x00 && data[end + 3] == 0x01) {
                        break;
                    }
                }

                int type = (data[i + 4] >> 1) & 0x3f;
                switch (type) {
                    case 32: // VPS
                        mVPS = createCSD(data, i, end);
                        break;
                    case 33: // SPS
                        mSPS = createCSD(data, i, end);
                        break;
                    case 34: // PPS
                        mPPS = createCSD(data, i, end);
                        break;
                }
            }
        }
        return mVPS != null && mSPS != null && mPPS != null;
    }

    private byte[] createCSD(byte[] data, int start, int end) {
        int length = end - start;
        byte[] csd = new byte[length];
        System.arraycopy(data, start, csd, 0, length);
        if (DEBUG) {
            StringBuilder builder = new StringBuilder();
            builder.append("csd: ");
            for (int i = start; i < end; i++) {
                if (i > start) {
                    builder.append(",");
                }
                builder.append(String.format("%02X", data[i]));
            }
            Log.d(TAG, builder.toString());
        }
        return csd;
    }

    /**
     * Surface に描画を行うスレッドを作成します.
     */
    private void createWorkThread() {
        if (mWorkThread != null) {
            mWorkThread.terminate();
        }

        if (mFrameCache != null) {
            mFrameCache.freeFrames();
        }
        mFrameCache = new FrameCache();
        mFrameCache.initFrames();

        mWorkThread = new WorkThread();
        mWorkThread.setName("H265-DECODER");
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

        /**
         * SPS のデータから映像の解像度を取得します.
         */
        private void parseSps() {
            try {
                mWidth = 320;
                mHeight = 480;
                H265Parser.Sps sps = H265Parser.parseSps(mSPS, 4);
                mWidth = sps.getWidth();
                mHeight = sps.getHeight();
            } catch (Exception e) {
                // ignore.
            }
        }

        /**
         * MediaCodec を作成します.
         *
         * @throws IOException MediaCodec の作成に失敗した場合に発生
         */
        private void createMediaCodec() throws IOException {
            parseSps();

            MediaFormat format = MediaFormat.createVideoFormat(mMimeType, mWidth, mHeight);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                format.setInteger(MediaFormat.KEY_COLOR_RANGE, MediaFormat.COLOR_RANGE_FULL);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                format.setInteger(MediaFormat.KEY_OPERATING_RATE, Short.MAX_VALUE);
            }

            ByteBuffer csd0 = ByteBuffer.allocateDirect(mVPS.length + mSPS.length + mPPS.length);
            csd0.put(mVPS);
            csd0.position(mVPS.length);
            csd0.put(mSPS);
            csd0.position(mVPS.length + mSPS.length);
            csd0.put(mPPS);

            format.setByteBuffer("csd-0", csd0);

            if (mMediaCodec != null) {
                try {
                    mMediaCodec.stop();
                    mMediaCodec.release();
                } catch (Exception e) {
                    // ignore.
                }
            }

            mMediaCodec = MediaCodec.createDecoderByType(mMimeType);
            mMediaCodec.configure(format, getSurface(), null, 0);
            mMediaCodec.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            mMediaCodec.start();
        }

        /**
         * MediaCodec を解放します.
         */
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
            }
            mVPS = null;
            mSPS = null;
            mPPS = null;
        }

        @Override
        public void run() {
            try {
                createMediaCodec();

                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

                while (!isInterrupted()) {
                    Frame frame = get();

                    int inIndex = mMediaCodec.dequeueInputBuffer(TIMEOUT_US);
                    if (inIndex >= 0) {
                        ByteBuffer buffer = mMediaCodec.getInputBuffer(inIndex);
                        if (buffer != null) {
                            buffer.clear();
                            buffer.put(frame.getBuffer(), 0, frame.getLength());
                            buffer.flip();
                        }

                        int flags = 0;
                        if (frame.getLength() > 4) {
                            int type = (frame.getBuffer()[4] >> 1) & 0x3F;
                            if (type == 32 || type == 33 || type == 34) {
                                flags = MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
                            }
                        }

                        mMediaCodec.queueInputBuffer(inIndex, 0, frame.getLength(), frame.getPTS(), flags);
                    }

                    frame.release();

                    int outIndex = mMediaCodec.dequeueOutputBuffer(info, TIMEOUT_US);
                    if (outIndex >= 0) {
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
                                MediaFormat mf = mMediaCodec.getOutputFormat();
                                int w = mf.getInteger(MediaFormat.KEY_WIDTH);
                                int h = mf.getInteger(MediaFormat.KEY_HEIGHT);
                                postSizeChanged(w, h);
                                break;

                            case MediaCodec.INFO_TRY_AGAIN_LATER:
                                if (DEBUG) {
                                    Log.d(TAG, "INFO_TRY_AGAIN_LATER");
                                    Log.d(TAG, "dequeueOutputBuffer timed out!");
                                }
                                Thread.sleep(30);
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
                    Log.w(TAG, "H264 encode occurred an exception.", e);
                }
                postError(e);
            } finally {
                releaseMediaCodec();
            }
        }
    }
}
