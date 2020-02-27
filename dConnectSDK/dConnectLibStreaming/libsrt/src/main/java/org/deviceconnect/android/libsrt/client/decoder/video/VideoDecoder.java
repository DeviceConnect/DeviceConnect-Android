package org.deviceconnect.android.libsrt.client.decoder.video;

import android.view.Surface;

import org.deviceconnect.android.libsrt.client.decoder.Decoder;

public abstract class VideoDecoder implements Decoder {
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
    public Surface getSurface() {
        return mSurface;
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
