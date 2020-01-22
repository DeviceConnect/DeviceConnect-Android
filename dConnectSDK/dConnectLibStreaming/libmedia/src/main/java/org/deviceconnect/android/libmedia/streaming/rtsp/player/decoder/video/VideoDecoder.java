package org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.video;

import android.view.Surface;

import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.Decoder;

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
     * デコード先のSurfaceを設定します.
     *
     * @param surface Surface
     */
    public abstract void setSurface(Surface surface);

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

    public interface EventCallback {
        void onSizeChanged(int width, int height);
    }
}
