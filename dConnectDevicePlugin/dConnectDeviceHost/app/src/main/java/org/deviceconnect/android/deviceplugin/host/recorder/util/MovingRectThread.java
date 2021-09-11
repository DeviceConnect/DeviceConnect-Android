package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.graphics.Rect;

import org.deviceconnect.android.libmedia.streaming.util.QueueThread;
import org.deviceconnect.android.libmedia.streaming.util.WeakReferenceList;

public class MovingRectThread {
    private final WeakReferenceList<OnEventListener> mOnEventListeners = new WeakReferenceList<>();
    private WorkThread mWorkThread;
    private int mFrameRate = 30;

    /**
     * スレッドを動作させるフレームレートを設定します.
     *
     * @param frameRate フレームレート
     */
    public void setFrameRate(int frameRate) {
        if (frameRate <= 0) {
            throw new IllegalArgumentException("frameRate is negative or zero.");
        }
        if (frameRate >= 1000) {
            throw new IllegalArgumentException("frameRate is over 1000.");
        }
        mFrameRate = frameRate;
    }

    /**
     * 範囲指定の移動処理を開始します.
     */
    public synchronized void start() {
        if (mWorkThread != null) {
            return;
        }
        mWorkThread = new WorkThread();
        mWorkThread.setName("MovingRectThread");
        mWorkThread.start();
    }

    /**
     * 範囲指定の移動処理を停止します.
     */
    public synchronized void stop() {
        if (mWorkThread != null) {
            mWorkThread.terminate();
            mWorkThread = null;
        }
    }

    /**
     * 矩形を設定します.
     *
     * @param rect 矩形
     */
    public void set(Rect rect) {
        move(null, rect, 0);
    }

    /**
     * 描画範囲の移動を設定します.
     *
     * @param start 開始する矩形
     * @param end 停止する矩形
     * @param duration 移動時間(ミリ秒)
     * @param cancel 移動中の処理をキャンセルするか設定
     */
    public synchronized void move(Rect start, Rect end, int duration, boolean cancel) {
        if (mWorkThread == null) {
            return;
        }

        if (duration < 0 || end == null) {
            return;
        }

        if (cancel) {
            mWorkThread.cancel();
        }

        MovingObject object = new MovingObject();
        object.mStartRect = start;
        object.mEndRect = end;
        object.mDuration = duration;
        object.mInterval = 1000 / mFrameRate;
        mWorkThread.add(object);
    }

    /**
     * 描画範囲の移動を設定します.
     *
     * 追加された時にアニメーション中の場合には、移動をキャンセルして、次の移動を開始します。
     *
     * @param start 開始する矩形
     * @param end 停止する矩形
     * @param duration 移動時間(ミリ秒)
     */
    public void move(Rect start, Rect end, int duration) {
        move(start, end, duration, true);
    }

    /**
     * 範囲の移動イベントを通知するリスナーを設定します.
     *
     * @param listener 追加するリスナー
     */
    public void addOnEventListener(OnEventListener listener) {
        if (listener != null) {
            mOnEventListeners.add(listener);
        }
    }

    /**
     * 範囲の移動イベントを通知するリスナーを削除します.
     *
     * @param listener 削除するリスナー
     */
    public void removeOnEventListener(OnEventListener listener) {
        if (listener != null) {
            mOnEventListeners.remove(listener);
        }
    }

    public interface OnEventListener {
        void onMoved(Rect rect);
    }

    private void postOnMoved(Rect rect) {
        for (OnEventListener cb : mOnEventListeners) {
            try {
                cb.onMoved(rect);
            } catch (Exception e) {
                // ignore.
            }
        }
    }

    private class MovingObject implements Runnable {
        private boolean mStopFlag = false;
        private final Rect mRect = new Rect();
        private Rect mStartRect;
        private Rect mEndRect;
        private int mDuration;
        private int mInterval;

        public void terminate() {
            mStopFlag = true;
        }

        @Override
        public void run() {
            int count = mDuration / mInterval;
            if (count > 0 && mStartRect != null) {
                float c = mDuration / (float) mInterval;
                float deltaLeft = (mEndRect.left - mStartRect.left) / c;
                float deltaTop = (mEndRect.top - mStartRect.top) / c;
                float deltaRight = (mEndRect.right - mStartRect.right) / c;
                float deltaBottom = (mEndRect.bottom - mStartRect.bottom) / c;

                float left = mStartRect.left;
                float top = mStartRect.top;
                float right = mStartRect.right;
                float bottom = mStartRect.bottom;

                while (!mStopFlag && count > 0) {
                    left += deltaLeft;
                    top += deltaTop;
                    right += deltaRight;
                    bottom += deltaBottom;
                    mRect.set((int) left, (int) top, (int) right, (int) bottom);
                    count--;

                    postOnMoved(mRect);

                    try {
                        Thread.sleep(mInterval);
                    } catch (Exception e) {
                        return;
                    }
                }
            }

            if (count == 0) {
                mRect.set(mEndRect);
                postOnMoved(mRect);
            }
        }
    }

    private static class WorkThread extends QueueThread<MovingObject> {
        private boolean mStopFlag = false;
        private MovingObject mMovingObject;

        private void cancel() {
            if (mMovingObject != null) {
                mMovingObject.terminate();
            }
        }

        /**
         * スレッドを終了します.
         */
        private void terminate() {
            mStopFlag = true;
            cancel();
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
                while (!mStopFlag) {
                    mMovingObject = get();
                    mMovingObject.run();
                }
            } catch (InterruptedException e) {
                // ignore.
            }
        }
    }
}
