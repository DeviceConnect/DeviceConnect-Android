package org.deviceconnect.android.libmedia.streaming.gles;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;

import org.deviceconnect.android.libmedia.streaming.util.WeakReferenceList;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class EGLSurfaceDrawingThread extends Thread {
    /**
     * 停止フラグ.
     */
    private boolean mStopFlag = true;

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
     * イベントを通知するリスナー.
     */
    private final WeakReferenceList<OnDrawingEventListener> mOnDrawingEventListeners = new WeakReferenceList<>();

    /**
     * イベントを通知するリスナーを追加します.
     *
     * @param listener リスナー
     */
    public void addOnDrawingEventListener(OnDrawingEventListener listener) {
        mOnDrawingEventListeners.add(listener);
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
     * 描画を行う Surface のサイズを設定します.
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
     * {@link #start()} で開始されて、{link #onStarted()} が呼び出された後でないとエラーが発生します。
     *
     * @param surface 描画を行う Surface
     * @return EGLSurfaceBase のインスタンス
     */
    public EGLSurfaceBase createEGLSurfaceBase(Surface surface) {
        return new WindowSurface(mEGLCore, surface);
    }

    /**
     * オフスクリーン用の EGLSurfaceBase を作成します.
     *
     * {@link #start()} で開始されて、{link #onStarted()} が呼び出された後でないとエラーが発生します。
     *
     * @param width 横幅
     * @param height 縦幅
     * @return EGLSurfaceBase のインスタンス
     */
    public EGLSurfaceBase createEGLSurfaceBase(int width, int height) {
        return new OffscreenSurface(mEGLCore, width, height);
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
            mEGLSurfaceBases.add(eglSurfaceBase);
        }
    }

    /**
     * 描画先の EGLSurfaceBase を削除します.
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
        if (tag == null) {
            return;
        }

        synchronized (mEGLSurfaceBases) {
            for (EGLSurfaceBase eglSurfaceBase : mEGLSurfaceBases) {
                if (tag.equals(eglSurfaceBase.getTag())) {
                    mEGLSurfaceBases.remove(eglSurfaceBase);
                    eglSurfaceBase.release();
                    return;
                }
            }
        }
    }

    /**
     * 登録されている Surface の個数を取得します.
     *
     * @return 登録されている Surface の個数
     */
    public int getSurfaceSize() {
        return mEGLSurfaceBases.size();
    }

    /**
     * 描画スレッドの動作状況を確認します.
     *
     * @return 動作中の場合はtrue、それ以外はfalse
     */
    public boolean isRunning() {
        return !mStopFlag;
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

    @Override
    public void start() {
        super.start();
        mStopFlag = false;
    }

    /**
     * スレッドを終了します.
     */
    public void terminate() {
        mStopFlag = true;

        interrupt();

        try {
            join(500);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    /**
     * SurfaceTextureManager を作成します.
     *
     * @return SurfaceTextureManager のインスタンス
     */
    public SurfaceTextureManager createStManager() {
        SurfaceTextureManager manager = new SurfaceTextureManager();
        // SurfaceTexture に解像度を設定
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
            l.onStarted();
        }
    }

    private void postOnStopped() {
        for (OnDrawingEventListener l : mOnDrawingEventListeners) {
            l.onStopped();
        }
    }

    protected void postOnError(Exception e) {
        for (OnDrawingEventListener l : mOnDrawingEventListeners) {
            l.onError(e);
        }
    }

    private void postOnDrawn(EGLSurfaceBase eglSurfaceBase) {
        for (OnDrawingEventListener l : mOnDrawingEventListeners) {
            l.onDrawn(eglSurfaceBase);
        }
    }

    @Override
    public void run() {
        try {
            mEGLCore = new EGLCore();
            mEGLCore.makeCurrent();

            mStManager = createStManager();

            onStarted();

            postOnStarted();

            SurfaceTexture st = mStManager.getSurfaceTexture();
            while (!mStopFlag) {
                mStManager.awaitNewImage();

                synchronized (mEGLSurfaceBases) {
                    for (EGLSurfaceBase eglSurfaceBase : mEGLSurfaceBases) {
                        eglSurfaceBase.makeCurrent();
                        mStManager.setViewport(0, 0, eglSurfaceBase.getWidth(), eglSurfaceBase.getHeight());
                        mStManager.drawImage(getDisplayRotation());
                        eglSurfaceBase.setPresentationTime(st.getTimestamp());
                        eglSurfaceBase.swapBuffers();
                        postOnDrawn(eglSurfaceBase);
                    }
                }
            }
        } catch (Exception e) {
            if (!mStopFlag) {
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

            postOnStopped();

            onStopped();
        }
    }

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
}
