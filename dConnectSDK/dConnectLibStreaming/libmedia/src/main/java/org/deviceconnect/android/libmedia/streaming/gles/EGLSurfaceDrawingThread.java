package org.deviceconnect.android.libmedia.streaming.gles;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.view.Surface;

import org.deviceconnect.android.libmedia.streaming.util.WeakReferenceList;

import java.util.ArrayList;
import java.util.List;

public class EGLSurfaceDrawingThread {
    /**
     * OpenGLES のコンテキストなどを管理するクラス.
     */
    private EGLCore mEGLCore;

    /**
     * SurfaceTexture管理クラス.
     */
    private SurfaceTextureManager mStManager;

    /**
     * 描画先の EGLSurface のリスト.
     */
    private final List<EGLSurfaceBase> mEGLSurfaceBases = new ArrayList<>();

    /**
     * 描画を行う Surface の横幅.
     */
    private int mWidth;

    /**
     * 描画を行う Surface の縦幅.
     */
    private int mHeight;

    /**
     * レンダリングのタイムアウト(ミリ秒).
     */
    private int mRenderingTimeout = 10 * 1000;

    /**
     * イベントを通知するリスナー.
     */
    private final WeakReferenceList<OnDrawingEventListener> mOnDrawingEventListeners = new WeakReferenceList<>();

    /**
     * 描画を実行するスレッド.
     */
    private DrawingThread mDrawingThread;

    /**
     * イベントを通知するリスナーを追加します.
     *
     * @param listener リスナー
     */
    public void addOnDrawingEventListener(OnDrawingEventListener listener) {
        mOnDrawingEventListeners.add(listener);

        // すでに開始されている場合には、リスナーのイベントを呼び出します。
        if (isInitCompleted() && mEGLCore != null) {
            listener.onStarted();
        }
    }

    /**
     * イベントを通知するリスナーを削除します.
     *
     * @param listener リスナー
     */
    public void removeOnDrawingEventListener(OnDrawingEventListener listener) {
        mOnDrawingEventListeners.remove(listener);
    }

    /**
     * レンダリングのタイムアウトを設定します.
     *
     * 描画先の映像が更新されない場合にタイムアウトするまでの時間を設定します。
     *
     * 0 が指定された場合にはタイムアウトしません。
     *
     * @param timeout タイムアウト
     */
    public void setRenderingTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout cannot set negative value.");
        }
        mRenderingTimeout = timeout;
    }

    /**
     * レンダリングのタイムアウトを取得します.
     *
     * @return タイムアウト
     */
    public int getRenderingTimeout() {
        return mRenderingTimeout;
    }

    /**
     * カメラなどの映像ソースのサイズを設定します.
     *
     * 映像ソースのサイズと配信先のサイズから映像が崩れないように
     * アスペクト比を計算を行い描画を行います。
     *
     * @param width 横幅
     * @param height 縦幅
     */
    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    /**
     * 描画を行う Surface の横幅を取得します.
     *
     * @return 描画を行う Surface の横幅
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * 描画を行う Surface の縦幅を取得します.
     *
     * @return 描画を行う Surface の縦幅
     */
    public int getHeight() {
        return mHeight;
    }

    /**
     * Window 用の EGLSurfaceBase を作成します.
     *
     * @param surface 描画を行う Surface
     * @return EGLSurfaceBase のインスタンス
     */
    public EGLSurfaceBase createEGLSurfaceBase(Surface surface) {
        return new WindowSurface(surface);
    }

    /**
     * オフスクリーン用の EGLSurfaceBase を作成します.
     *
     * @param width 横幅
     * @param height 縦幅
     * @return EGLSurfaceBase のインスタンス
     */
    public EGLSurfaceBase createEGLSurfaceBase(int width, int height) {
        return new OffscreenSurface(width, height);
    }

    /**
     * 描画先の EGLSurfaceBase を追加します.
     *
     * 追加された EGLSurfaceBase は、終了時に全て削除されます。
     *
     * @param eglSurfaceBase 追加する EGLSurfaceBase
     */
    public void addEGLSurfaceBase(EGLSurfaceBase eglSurfaceBase) {
        synchronized (mEGLSurfaceBases) {
            if (mEGLCore != null) {
                eglSurfaceBase.initEGLSurfaceBase(mEGLCore);
            }
            mEGLSurfaceBases.add(eglSurfaceBase);
        }
    }

    /**
     * 描画先の EGLSurfaceBase を追加します.
     *
     * 引数に設定した Surface の EGLSurfaceBase を作成します。
     * また、タグには、引数に指定された surface を設定します。
     *
     * @param surface 追加する EGLSurfaceBase に設定する Surface
     */
    public void addEGLSurfaceBase(Surface surface) {
        addEGLSurfaceBase(surface, surface, null);
    }

    /**
     * 描画先の EGLSurfaceBase を追加します.
     *
     * 引数に設定した Surface の EGLSurfaceBase を作成します。
     * また、タグには、引数に指定された surface を設定します。
     *
     * 作成した EGLSurfaceBase に引数で指定された drawingRange を描画範囲に設定します。
     *
     * @param surface 追加する EGLSurfaceBase に設定する Surface
     * @param drawingRange 描画範囲
     */
    public void addEGLSurfaceBase(Surface surface, Rect drawingRange) {
        addEGLSurfaceBase(surface, surface, drawingRange);
    }

    /**
     * 描画先の EGLSurfaceBase を追加します.
     *
     * 引数に設定した Surface の EGLSurfaceBase を作成します。
     *
     * @param surface 追加する EGLSurfaceBase に設定する Surface
     */
    public void addEGLSurfaceBase(Surface surface, Object tag, Rect drawingRange) {
        EGLSurfaceBase eglSurfaceBase = createEGLSurfaceBase(surface);
        eglSurfaceBase.setTag(tag);
        eglSurfaceBase.setCropRect(drawingRange);
        addEGLSurfaceBase(eglSurfaceBase);
    }

    /**
     * 描画先の EGLSurfaceBase を追加します.
     *
     * 引数に指定された width と height でオフスクリーンの EGLSurfaceBase を作成します。
     *
     * @param width 横幅
     * @param height 縦幅
     * @param tag タグ
     */
    public void addEGLSurfaceBase(int width, int height, Object tag) {
        EGLSurfaceBase eglSurfaceBase = createEGLSurfaceBase(width, height);
        eglSurfaceBase.setTag(tag);
        addEGLSurfaceBase(width, height, tag, null);
    }

    /**
     * 描画先の EGLSurfaceBase を追加します.
     *
     * 引数に指定された width と height でオフスクリーンの EGLSurfaceBase を作成します。
     *
     * @param width 横幅
     * @param height 縦幅
     * @param tag タグ
     */
    public void addEGLSurfaceBase(int width, int height, Object tag, Rect drawingRange) {
        EGLSurfaceBase eglSurfaceBase = createEGLSurfaceBase(width, height);
        eglSurfaceBase.setTag(tag);
        eglSurfaceBase.setCropRect(drawingRange);
        addEGLSurfaceBase(eglSurfaceBase);
    }

    /**
     * 描画先の EGLSurfaceBase を削除します.
     *
     * 削除した EGLSurfaceBase は {@link EGLSurfaceBase#release()} を呼び出しますので
     * {@link #addEGLSurfaceBase(EGLSurfaceBase)} に指定するとエラーが発生します。
     *
     * EGLSurfaceBase は作り直してください。
     *
     * @param eglSurfaceBase 削除する EGLSurfaceBase
     */
    public void removeEGLSurfaceBase(EGLSurfaceBase eglSurfaceBase) {
        synchronized (mEGLSurfaceBases) {
            mEGLSurfaceBases.remove(eglSurfaceBase);
            eglSurfaceBase.release();
        }
    }

    /**
     * 指定されたタグと一致する EGLSurfaceBase を削除します.
     *
     * @param tag 削除する EGLSurfaceBase のタグ
     */
    public void removeEGLSurfaceBase(Object tag) {
        EGLSurfaceBase eglSurfaceBase = findEGLSurfaceBaseByTag(tag);
        if (eglSurfaceBase != null) {
            removeEGLSurfaceBase(eglSurfaceBase);
        }
    }

    /**
     * 指定されたタグと一致する EGLSurfaceBase を取得します.
     *
     * 一致する EGLSurfaceBase が見つからない場合には null を返却します。
     *
     * @param tag EGLSurfaceBase のタグ
     * @return EGLSurfaceBase のインスタンス
     */
    public EGLSurfaceBase findEGLSurfaceBaseByTag(Object tag) {
        if (tag != null) {
            synchronized (mEGLSurfaceBases) {
                for (EGLSurfaceBase eglSurfaceBase : mEGLSurfaceBases) {
                    if (tag.equals(eglSurfaceBase.getTag())) {
                        return eglSurfaceBase;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 登録されている Surface の個数を取得します.
     *
     * @return 登録されている Surface の個数
     */
    public int getEGLSurfaceBaseCount() {
        return mEGLSurfaceBases.size();
    }

    /**
     * 描画を行う SurfaceTexture を取得します.
     *
     * <p>
     * この SurfaceTexture に描画した内容をエンコードします。
     * </p>
     *
     * @return SurfaceTexture
     */
    public SurfaceTexture getSurfaceTexture() {
        return mStManager != null ? mStManager.getSurfaceTexture() : null;
    }

    /**
     * 描画スレッドの動作状況を確認します.
     *
     * @return 動作中の場合はtrue、それ以外はfalse
     */
    public boolean isRunning() {
        return mDrawingThread != null && mDrawingThread.isRunning();
    }

    /**
     * 初期化が完了しているか確認します.
     *
     * @return 初期化が完了している場合は true、それ以外はfalse
     */
    public boolean isInitCompleted() {
        return mDrawingThread != null && mDrawingThread.isInitCompleted();
    }

    /**
     * スレッドを開始します.
     *
     * 既に開始されている場合には何も処理を行いません。
     */
    public synchronized void start() {
        if (isRunning()) {
            return;
        }
        mDrawingThread = new DrawingThread();
        mDrawingThread.setName("Surface-Drawing-Thread");
        mDrawingThread.start();
    }

    /**
     * スレッドを終了します.
     *
     * 終了時に登録されている全ての GELSurfaceBase は削除します。
     *
     * EGLSurfaceBase が登録されていても強制的に終了します。
     */
    public void stop() {
        stop(true);
    }

    /**
     * スレッドを終了します.
     *
     * 引数に false が指定された場合には、{@link #getEGLSurfaceBaseCount()} の
     * 値が 0 に以外の場合には、スレッドを停止しません。EGLSurfaceBase が全て削除された状態でのみ停止します。
     *
     * 引数に true が指定された場合には、{@link #getEGLSurfaceBaseCount()} の
     * 値が 0 以上でも強制的に停止します。
     *
     * @param force 強制的に終了する場合は true、それ以外は false
     */
    public synchronized void stop(boolean force) {
        if (mDrawingThread != null) {
            if (force || getEGLSurfaceBaseCount() == 0) {
                mDrawingThread.terminate();
                mDrawingThread = null;
            }
        }
    }

    /**
     * SurfaceTextureManager を作成します.
     *
     * @return SurfaceTextureManager のインスタンス
     */
    protected SurfaceTextureManager createStManager() {
        SurfaceTextureManager manager = new SurfaceTextureManager();
        SurfaceTexture st = manager.getSurfaceTexture();
        st.setDefaultBufferSize(mWidth, mHeight);
        return manager;
    }

    /**
     * SurfaceTextureManager の後始末を行います.
     */
    private void releaseStManager() {
        if (mStManager != null) {
            mStManager.release();
            mStManager = null;
        }
    }

    /**
     * 画面の回転を取得します.
     *
     * 画面の回転を行いたい場合には、このクラスをオーバーライドします。
     *
     * 以下の値を返すこと。
     * <ul>
     * <li>Surface.ROTATION_0</li>
     * <li>Surface.ROTATION_90</li>
     * <li>Surface.ROTATION_180</li>
     * <li>Surface.ROTATION_270</li>
     * </ul>
     *
     * @return 画面の回転
     */
    public int getDisplayRotation() {
        return Surface.ROTATION_0;
    }

    /**
     * 画面の縦・横の切り替えを行うか確認します.
     *
     * @return 画面の縦・横を切り替える場合はtrue、それ以外はfalse
     */
    public boolean isSwappedDimensions() {
        switch (getDisplayRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                return false;
            default:
                return true;
        }
    }

    /**
     * スレッドが開始され SurfaceTextureManager が作成された後に呼び出されます.
     */
    protected void onStarted() {
    }

    /**
     * スレッドが停止された時に呼び出されます.
     */
    protected void onStopped() {
    }

    private void postOnStarted() {
        for (OnDrawingEventListener l : mOnDrawingEventListeners) {
            try {
                l.onStarted();
            } catch (Exception e) {
                // ignore.
            }
        }
    }

    private void postOnStopped() {
        for (OnDrawingEventListener l : mOnDrawingEventListeners) {
            try {
                l.onStopped();
            } catch (Exception e) {
                // ignore.
            }
        }
    }

    protected void postOnError(Exception e) {
        for (OnDrawingEventListener l : mOnDrawingEventListeners) {
            try {
                l.onError(e);
            } catch (Exception ex) {
                // ignore.
            }
        }
    }

    private void postOnDrawn(EGLSurfaceBase eglSurfaceBase) {
        for (OnDrawingEventListener l : mOnDrawingEventListeners) {
            try {
                l.onDrawn(eglSurfaceBase);
            } catch (Exception e) {
                // ignore.
            }
        }
    }

    /**
     * 描画処理を行うスレッド.
     */
    private class DrawingThread extends Thread {
        private static final int STATE_INIT = 1;
        private static final int STATE_RUNNING = 2;
        private static final int STATE_STOPPING = 3;
        private static final int STATE_STOPPED = 4;

        /**
         * スレッドの状態.
         */
        private int mState = STATE_INIT;

        /**
         * スレッドの動作を確認します.
         *
         * @return スレッドが動作中の場合は true、それ以外は false
         */
        private boolean isRunning() {
            return mState <= STATE_RUNNING;
        }

        /**
         * 初期化が完了しているか確認します.
         *
         * @return 初期化が完了している場合は true、それ以外はfalse
         */
        private boolean isInitCompleted() {
            return mState == STATE_RUNNING;
        }

        /**
         * スレッドを終了します.
         */
        private void terminate() {
            mState = STATE_STOPPING;

            interrupt();

            try {
                join(500);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        @Override
        public void run() {
            try {
                mEGLCore = new EGLCore();
                mEGLCore.makeCurrent();

                mStManager = createStManager();
                mStManager.setTimeout(mRenderingTimeout);

                synchronized (mEGLSurfaceBases) {
                    for (EGLSurfaceBase surfaceBase : mEGLSurfaceBases) {
                        surfaceBase.initEGLSurfaceBase(mEGLCore);
                    }
                }

                mState = STATE_RUNNING;

                onStarted();
                postOnStarted();

                ViewSize size = new ViewSize();
                SurfaceTexture st = mStManager.getSurfaceTexture();
                while (mState == STATE_RUNNING) {
                    calcFps();
                    mStManager.awaitNewImage();

                    synchronized (mEGLSurfaceBases) {
                        for (EGLSurfaceBase eglSurfaceBase : mEGLSurfaceBases) {
                            eglSurfaceBase.makeCurrent();
                            int viewportX = 0;
                            int viewportY = 0;
                            int viewportW = eglSurfaceBase.getWidth();
                            int viewportH = eglSurfaceBase.getHeight();
                            Rect cropRect = eglSurfaceBase.getCropRect();
                            if (cropRect != null) {
                                mStManager.setCropRect(cropRect, mWidth, mHeight);
                                calculateViewSize(cropRect.width(), cropRect.height(), viewportW, viewportH, size);
                            } else {
                                mStManager.clearCropRect();
                                calculateViewSize(mWidth, mHeight, viewportW, viewportH, size);
                            }
                            if (viewportW > size.getWidth()) {
                                viewportX = (viewportW - size.getWidth()) / 2;
                                viewportW = size.getWidth();
                            } else if (viewportH > size.getHeight()) {
                                viewportY = (viewportH - size.getHeight()) / 2;
                                viewportH = size.getHeight();
                            }
                            mStManager.setViewport(viewportX, viewportY,viewportW, viewportH);
                            mStManager.drawImage(getDisplayRotation());
                            eglSurfaceBase.setPresentationTime(st.getTimestamp());
                            eglSurfaceBase.swapBuffers();
                            postOnDrawn(eglSurfaceBase);
                        }
                    }
                }
            } catch (Exception e) {
                if (mState != STATE_STOPPING) {
                    postOnError(e);
                }
            } finally {
                synchronized (mEGLSurfaceBases) {
                    for (EGLSurfaceBase eglSurfaceBase : mEGLSurfaceBases) {
                        eglSurfaceBase.release();
                    }
                    mEGLSurfaceBases.clear();
                }

                releaseStManager();

                // EGLCore を release するタイミングは一番最後にすること。
                // 先に EGLSurfaceBase よりも先に release を行うと、EGLSurfaceBase
                // の release に失敗して、メモリリークになります。
                if (mEGLCore != null) {
                    mEGLCore.release();
                    mEGLCore = null;
                }

                mState = STATE_STOPPED;

                postOnStopped();

                try {
                    onStopped();
                } catch (Exception e) {
                    // ignore.
                }
            }
        }
    }

    private static class ViewSize {
        int mWidth;
        int mHeight;

        public int getWidth() {
            return mWidth;
        }

        public int getHeight() {
            return mHeight;
        }
    }

    /**
     * 指定された View のサイズにフィットするサイズを計算します.
     *
     * @param width 横幅
     * @param height 縦幅
     * @param viewWidth View のサイズ
     * @param viewHeight View のサイズ
     * @param dest 出力先
     */
    private void calculateViewSize(int width, int height, int viewWidth, int viewHeight, ViewSize dest) {
        int h =  (int) (height * (viewWidth / (float) width));
        if (viewHeight < h) {
            int w = (int) (width * (viewHeight / (float) height));
            if (w % 2 != 0) {
                w--;
            }
            dest.mWidth = w;
            dest.mHeight = viewHeight;
        } else {
            dest.mWidth = viewWidth;
            dest.mHeight = h;
        }
    }

    /**
     * 描画イベントを通知するリスナー.
     */
    public interface OnDrawingEventListener {
        /**
         * 描画の開始を通知します.
         */
        void onStarted();

        /**
         * 描画の停止を通知します.
         */
        void onStopped();

        /**
         * 描画中にエラーが発生したことを通知します.
         *
         * @param e エラー原因の例外
         */
        void onError(Exception e);

        /**
         * 描画が完了したことを通知します.
         *
         * @param eglSurfaceBase 描画が完了した EGLSurfaceBase
         */
        void onDrawn(EGLSurfaceBase eglSurfaceBase);
    }

    // FPS計測用のパラメータ
    private long mStartTime = 0;
    private int frameCount = 0;
    private float mFps = 0.0f;
    /**
     * FPSを計測する.
     */
    private void calcFps() {
        long currentTimeMillis = System.currentTimeMillis();
        if (mStartTime > 0) {
            long timeSpan = currentTimeMillis - mStartTime;
            frameCount++;

            // intervalとして500ms以上のとき
            if (timeSpan > 500) {
                mFps = frameCount * 1000 / (float) timeSpan;

                mStartTime = currentTimeMillis;
                frameCount = 0;
            }
        } else {
            mStartTime = currentTimeMillis;
        }
    }

    /**
     * FPSを取得する.
     * @return fps
     */
    public float getFps() {
        return mFps;
    }
}
