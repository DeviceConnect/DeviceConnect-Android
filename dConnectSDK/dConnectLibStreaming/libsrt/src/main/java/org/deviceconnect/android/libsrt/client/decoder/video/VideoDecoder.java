package org.deviceconnect.android.libsrt.client.decoder.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import org.deviceconnect.android.libmedia.streaming.util.QueueThread;
import org.deviceconnect.android.libsrt.BuildConfig;
import org.deviceconnect.android.libsrt.client.Frame;
import org.deviceconnect.android.libsrt.client.FrameCache;
import org.deviceconnect.android.libsrt.client.decoder.Decoder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

public abstract class VideoDecoder implements Decoder {
    /**
     * デバッグ用フラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "VideoDecoder";

    /**
     * エラー通知用のリスナー.
     */
    private ErrorCallback mErrorCallback;

    /**
     * イベント通知用のリスナー.
     */
    private EventCallback mEventCallback;

    /**
     * 映像を描画する Surface.
     */
    private Surface mSurface;

    /**
     * フレームをキャッシュを管理するクラス.
     */
    private FrameCache mFrameCache;

    /**
     * MediaCodec を作成する前に送られてきたフレームを保持するためのキュー.
     */
    private Queue<Frame> mFrameQueue = new ArrayDeque<>();

    /**
     * デコード処理を行うスレッド.
     */
    private WorkThread mWorkThread;

    @Override
    public void onInit() {
        if (mFrameCache != null) {
            mFrameCache.freeFrames();
        }
        mFrameCache = new FrameCache();
        mFrameCache.initFrames();
        mFrameQueue.clear();
    }

    @Override
    public void onReceived(byte[] data, int dataLength, long pts) {
        pts = (long) ((pts / (float) 90000) * 1000 * 1000);

        if (isRunningWorkThread()) {
            Frame frame = mFrameCache.getFrame(data, dataLength, pts);
            if (frame == null) {
                return;
            }
            mWorkThread.add(frame);
        } else {
            Frame frame = mFrameCache.getFrame(data, dataLength, pts);
            if (frame == null) {
                mFrameCache.releaseAllFrames();
                frame = mFrameCache.getFrame(data, dataLength, pts);
            }
            mFrameQueue.add(frame);

            if (searchConfig(data, dataLength)) {
                createWorkThread();

                for (Frame f : mFrameQueue) {
                    mWorkThread.add(f);
                }

                // 事前に送られてきたデータは削除しておく
                mFrameQueue.clear();
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

    @Override
    public void setErrorCallback(ErrorCallback callback) {
        mErrorCallback = callback;
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
     * 描画先の Surface を設定します.
     *
     * @param surface 描画先の Surface
     */
    public void setSurface(Surface surface) {
        mSurface = surface;
    }

    /**
     * 描画先の Surface を取得します.
     *
     * @return 描画先の Surface
     */
    Surface getSurface() {
        return mSurface;
    }

    /**
     * エラー通知を行う.
     *
     * @param e 例外
     */
    private void postError(final Exception e) {
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
    private void postSizeChanged(int width, int height) {
        if (mEventCallback != null) {
            mEventCallback.onSizeChanged(width, height);
        }
    }

    /**
     * MediaCodec に渡す映像情報を探します.
     *
     * @param data 送られてきたデータ
     * @param dataLength データサイズ
     * @return MediaCodec を作成する情報が集まった場合はtrue、それ以外はfalse
     */
    protected abstract boolean searchConfig(byte[] data, int dataLength);

    /**
     * MediaCodec を作成します.
     *
     * @return MediaCodec
     * @throws IOException MediaCodec の作成に失敗した場合に発生
     */
    protected abstract MediaCodec createMediaCodec() throws IOException;

    /**
     * 送られてきたフレームのフラグを取得します.
     *
     * @param data 送られてきたデータ
     * @param dataLength データサイズ
     * @return フラグ
     */
    protected abstract int getFlags(byte[] data, int dataLength);

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

        /**
         * MediaCodec を解放します.
         */
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
                        if (buffer != null) {
                            buffer.clear();
                            buffer.put(frame.getBuffer(), 0, frame.getLength());
                            buffer.flip();
                        }

                        int flags = 0;
                        if (frame.getLength() > 4) {
                            flags = getFlags(frame.getBuffer(), frame.getLength());
                        }

                        mMediaCodec.queueInputBuffer(inIndex, 0, frame.getLength(), frame.getPTS(), flags);
                    }

                    frame.release();

                    int outIndex = mMediaCodec.dequeueOutputBuffer(info, TIMEOUT_US);
                    if (outIndex >= 0 && !mStopFlag) {
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

    /**
     * VideoDecoder で発生したイベントを通知するコールバック.
     */
    public interface EventCallback {
        /**
         * 映像の解像度が変更されたことを通知します.
         *
         * @param width 横幅
         * @param height 縦幅
         */
        void onSizeChanged(int width, int height);
    }
}
