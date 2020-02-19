package org.deviceconnect.android.libsrt.client.decoder.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.video.H264Parser;
import org.deviceconnect.android.libsrt.BuildConfig;
import org.deviceconnect.android.libsrt.client.Frame;
import org.deviceconnect.android.libsrt.client.FrameCache;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.Queue;

public class H264Decoder extends VideoDecoder {
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
    private String mMimeType = "video/avc";

    /**
     * MediaCodec に渡す横幅.
     */
    private int mWidth;

    /**
     * MediaCodec に渡す縦幅.
     */
    private int mHeight;

    /**
     * SPSの情報を格納するバッファ.
     */
    private ByteBuffer mCsd0;

    /**
     * PPSの情報を格納するバッファ.
     */
    private ByteBuffer mCsd1;

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
        if (isRunningWorkThread()) {
            Frame frame = mFrameCache.getFrame(data, dataLength, (pts / 90000) * 1000 * 1000);
            if (frame == null) {
                if (DEBUG) {
                    Log.e(TAG, "No free frame.");
                }
                return;
            }
            mWorkThread.add(frame);
        } else if (searchSPSandPPS(data, dataLength)) {
            createWorkThread();
        }
    }

    @Override
    public void onReleased() {
        if (mWorkThread != null) {
            mWorkThread.terminate();
            mWorkThread = null;
        }
    }

    /**
     * SPS と PPS のパケットを探して、csd-0 と csd-1 のデータを作成します.
     *
     * @param data フレームバッファ
     * @return SPSとPPSが作成できた場合はtrue、それ以外はfalse
     */
    private boolean searchSPSandPPS(byte[] data, int dataLength) {
        for (int i = 0; i + 5 < dataLength; i++) {
            if (data[i] == 0x00 && data[i + 1] == 0x00 && data[i + 2] == 0x00 && data[i + 3] == 0x01) {
                int end = i + 4;
                for (; end + 4 < dataLength; end++) {
                    if (data[end] == 0x00 && data[end + 1] == 0x00 && data[end + 2] == 0x00 && data[end + 3] == 0x01) {
                        break;
                    }
                }
                int type = data[i + 4] & 0x1F;
                switch (type) {
                    case 7: // SPS
                        createCSD0(data, i + 4, end);
                        break;
                    case 8: // PPS
                        createCSD1(data, i + 4, end);
                        break;
                }
            }
        }

        return mCsd0 != null && mCsd1 != null;
    }

    /**
     * SPS (csd-0) のデータを作成します.
     *
     * @param data データが格納されたフレームバッファ
     * @param start csd-0 開始位置
     * @param end csd-0 終了位置
     */
    private void createCSD0(byte[] data, int start, int end) {
        if (DEBUG) {
            StringBuilder builder = new StringBuilder();
            builder.append("csd-0: ");
            for (int i = start; i < end; i++) {
                if (i > start) {
                    builder.append(",");
                }
                builder.append(String.format("%02X", data[i]));
            }
            Log.d(TAG, builder.toString());
        }

        mCsd0 = ByteBuffer.allocateDirect(end - start).order(ByteOrder.nativeOrder());
        mCsd0.put(data, start, end - start);
        mCsd0.flip();
    }

    /**
     * PPS (csd-1) のデータを作成します.
     *
     * @param data データが格納されたフレームバッファ
     * @param start csd-1 開始位置
     * @param end csd-1 終了位置
     */
    private void createCSD1(byte[] data, int start, int end) {
        if (DEBUG) {
            StringBuilder builder = new StringBuilder();
            builder.append("csd-1: ");
            for (int i = start; i < end; i++) {
                if (i > start) {
                    builder.append(",");
                }
                builder.append(String.format("%02X", data[i]));
            }
            Log.d(TAG, builder.toString());
        }

        mCsd1 = ByteBuffer.allocateDirect(end - start).order(ByteOrder.nativeOrder());
        mCsd1.put(data, start, end - start);
        mCsd1.flip();
    }

    /**
     * Surface に描画を行うスレッドを作成します.
     */
    private void createWorkThread() {
        if (mWorkThread != null) {
            mWorkThread.terminate();
        }

        mFrameCache = new FrameCache();
        mFrameCache.initFrames();

        mWorkThread = new WorkThread();
        mWorkThread.setName("GoProLiveStreaming-WorkThread");
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
    private class WorkThread extends Thread {
        /**
         * タイムアウト時間を定義.
         */
        private static final long TIMEOUT_US = 50000;

        /**
         * 送られてきたデータを格納するリスト.
         */
        private final Queue<Frame> mFrames = new LinkedList<>();

        /**
         * データの終了フラグ.
         */
        private boolean isEOS = false;

        /**
         * デコードを行うMediaCodec.
         */
        private MediaCodec mMediaCodec;

        /**
         * スレッドのクローズ処理を行います.
         */
        void terminate() {
            isEOS = true;

            interrupt();

            try {
                join(400);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        /**
         * 送られてきたデータを通知します.
         *
         * @param frame フレームバッファ
         */
        synchronized void add(Frame frame) {
            mFrames.offer(frame);
            notifyAll();
        }

        /**
         * 送られてきたデータを取得します.
         *
         * @return 送られてきたデータ
         * @throws InterruptedException スレッドがインタラプトされた場合に発生
         */
        private synchronized Frame getFrame() throws InterruptedException {
            while (mFrames.peek() == null) {
                wait();
            }
            return mFrames.remove();
        }

        /**
         * SPS のデータから映像の解像度を取得します.
         */
        private void parseSps() {
            byte[] spsData = new byte[mCsd0.limit()];
            mCsd0.get(spsData);
            mCsd0.flip();

            H264Parser.Sps sps = H264Parser.parseSps(spsData, 0);
            mWidth = sps.getWidth();
            mHeight = sps.getHeight();
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

            format.setByteBuffer("csd-0", mCsd0);
            format.setByteBuffer("csd-1", mCsd1);

            if (mMediaCodec != null) {
                mMediaCodec.stop();
                mMediaCodec.release();
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
                    mMediaCodec.release();
                } catch (Exception e) {
                    // ignore.
                }
            }
            mCsd0 = null;
            mCsd1 = null;
        }

        @Override
        public void run() {
            try {
                createMediaCodec();

                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                long startMs = System.currentTimeMillis();

                while (!isInterrupted()) {
                    Frame frame = getFrame();

                    if (!isEOS) {
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
                                int type = frame.getBuffer()[4] & 0x1F;
                                if (type == 0x07 || type == 0x08) {
                                    flags = MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
                                    if (type == 0x07) {
                                        try {
                                            H264Parser.Sps sps = H264Parser.parseSps(frame.getBuffer(), 4);
                                            postSizeChanged(sps.getWidth(), sps.getHeight());
                                        } catch (Exception e) {
                                            if (DEBUG) {
                                                Log.e(TAG, "", e);
                                            }
                                        }
                                    }
                                }
                            }

                            mMediaCodec.queueInputBuffer(inIndex, 0, frame.getLength(), frame.getPTS(), flags);
                        }
                    }

                    frame.release();

                    int outIndex = mMediaCodec.dequeueOutputBuffer(info, TIMEOUT_US);
                    if (outIndex >= 0) {
                        // We use a very simple clock to keep the video FPS, or the video
                        // playback will be too fast
                        while ((info.presentationTimeUs / 1000) > (System.currentTimeMillis() - startMs)) {
                            try {
                                sleep(10);
                            } catch (InterruptedException e) {
                                break;
                            }
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

                    // All decoded frames have been rendered, we can stop playing now
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if (DEBUG) {
                            Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                        }
                        break;
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
