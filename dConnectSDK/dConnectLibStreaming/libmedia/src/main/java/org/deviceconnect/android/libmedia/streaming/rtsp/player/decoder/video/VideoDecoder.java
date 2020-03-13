package org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.rtp.RtpDepacketize;
import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.Decoder;
import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.Frame;
import org.deviceconnect.android.libmedia.streaming.sdp.MediaDescription;
import org.deviceconnect.android.libmedia.streaming.util.QueueThread;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class VideoDecoder implements Decoder {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "RTSP-DECODE";

    /**
     * エラー通知用のリスナー.
     */
    private ErrorCallback mErrorCallback;

    /**
     * イベント通知用のリスナー.
     */
    private EventCallback mEventCallback;

    /**
     * デコード処理を行うスレッド.
     */
    private WorkThread mWorkThread;

    /**
     * 描画先のSurface.
     */
    private Surface mSurface;

    /**
     * RTP をデコードするためのクラス.
     */
    private RtpDepacketize mDepacketize;

    private int mClockFrequency;
    private Frame mConfigFrame;

    @Override
    public void onInit(MediaDescription md) {
        mClockFrequency = 90000;

        configure(md);
        createWorkThread();

//        mWorkThread.add(mConfigFrame);

        mDepacketize = createDepacketize();
        mDepacketize.setClockFrequency(mClockFrequency);
        mDepacketize.setCallback((data, pts) -> {
            if (mWorkThread != null) {
                mWorkThread.add(new Frame(data, pts));
            }
        });
    }

    @Override
    public void onRtpReceived(MediaDescription md, byte[] data, int dataLength) {
        if (mDepacketize != null) {
            mDepacketize.write(data, dataLength);
        }
    }

    @Override
    public void onRtcpReceived(MediaDescription md, byte[] data, int dataLength) {
    }

    @Override
    public void onRelease() {
        if (mWorkThread != null) {
            mWorkThread.terminate();
            mWorkThread = null;
        }

        if (mDepacketize != null) {
            mDepacketize = null;
        }
    }

    @Override
    public void setErrorCallback(final ErrorCallback listener) {
        mErrorCallback = listener;
    }

    /**
     * イベント通知用のコールバックを設定します.
     *
     * @param eventCallback コールバック
     */
    public void setEventCallback(EventCallback eventCallback) {
        mEventCallback = eventCallback;
    }

    /**
     * デコード先のSurfaceを設定します.
     *
     * @param surface Surface
     */
    public void setSurface(Surface surface) {
        mSurface = surface;
    }

    /**
     * デコード先のSurfaceを取得します.
     *
     * @return デコード先のSurface
     */
    Surface getSurface() {
        return mSurface;
    }

    /**
     * Clock Frequency を設定します.
     *
     * @param clockFrequency Clock Frequency
     */
    void setClockFrequency(int clockFrequency) {
        mClockFrequency = clockFrequency;
    }

    /**
     * メディアデータのコンフィグ用のフレームを設定します.
     *
     * @param configFrame フレーム
     */
    void setConfigFrame(Frame configFrame) {
        mConfigFrame = configFrame;
    }

    /**
     * エラー通知を行う.
     *
     * @param e 例外
     */
    void postError(final Exception e) {
        if (mErrorCallback != null) {
            mErrorCallback.onError(e);
        }
    }

    /**
     * 映像のサイズ変更を通知します.
     *
     * @param width 横幅
     * @param height 縦幅
     */
    void postSizeChanged(int width, int height) {
        if (mEventCallback != null) {
            mEventCallback.onSizeChanged(width, height);
        }
    }

    /**
     * Surface に描画を行うスレッドを作成します.
     */
    private void createWorkThread() {
        if (mWorkThread != null) {
            mWorkThread.terminate();
        }

        mWorkThread = new WorkThread();
        mWorkThread.setName("VIDEO-DECODER");
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
     * MediaDescription から MediaCodec の設定を取得します.
     *
     * @param md MediaDescription
     */
    protected abstract void configure(MediaDescription md);

    /**
     * RTP のパケットをデパケットするクラスを作成します.
     *
     * @return RtpDepacketize の実装クラス
     */
    protected abstract RtpDepacketize createDepacketize();

    /**
     * MediaCodecを作成します.
     *
     * @throws IOException MediaCodecの作成に失敗した場合に発生
     */
    protected abstract MediaCodec createMediaCodec() throws IOException;

    /**
     * 送られてきたフレームのフラグを取得します.
     *
     * @param data データ
     * @param dataLength データサイズ
     * @return フラグ
     */
    protected abstract int getFlags(byte[] data, int dataLength);

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

                mMediaCodec = createMediaCodec();

                while (!mStopFlag) {
                    Frame frame = get();

                    int inIndex = mMediaCodec.dequeueInputBuffer(TIMEOUT_US);
                    if (inIndex >= 0 && !mStopFlag) {
                        ByteBuffer buffer = mMediaCodec.getInputBuffer(inIndex);
                        if (buffer == null) {
                            continue;
                        }

                        buffer.clear();
                        buffer.put(frame.getData(), 0, frame.getLength());
                        buffer.flip();

                        int flags = 0;
                        if (frame.getLength() > 4) {
                            flags = getFlags(frame.getData(), frame.getLength());
                        }

                        mMediaCodec.queueInputBuffer(inIndex, 0, frame.getLength(), frame.getTimestamp(), flags);
                    }

                    int outIndex = mMediaCodec.dequeueOutputBuffer(info, TIMEOUT_US);
                    if (outIndex > 0 && !mStopFlag) {
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
                                Thread.sleep(1);
                                break;

                            default:
                                break;
                        }
                    }
                }
            } catch (InterruptedException e) {
                // ignore.
            } catch (Exception e) {
                if (DEBUG) {
                    Log.w(TAG, "video decoder occurred an exception.", e);
                }
                if (!mStopFlag) {
                    postError(e);
                }
            } finally {
                releaseMediaCodec();
            }
        }
    }

    public interface EventCallback {
        void onSizeChanged(int width, int height);
    }
}
