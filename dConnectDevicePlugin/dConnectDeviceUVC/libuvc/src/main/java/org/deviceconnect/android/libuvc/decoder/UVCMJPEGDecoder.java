/*
 UVCMJPEGDecoder.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libuvc.decoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.Surface;

import org.deviceconnect.android.libuvc.Frame;
import org.deviceconnect.android.libuvc.Parameter;
import org.deviceconnect.android.libuvc.UVCCamera;

/**
 * MotionJPEGをデコードしてSurfaceに描画するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
class UVCMJPEGDecoder implements UVCDecoder {
    /**
     * 描画先の Surface.
     */
    private Surface mSurface;

    /**
     * イベントを通知するリスナー.
     */
    private OnEventListener mOnEventListener;

    @Override
    public void setSurface(final Surface surface) {
        mSurface = surface;
    }

    @Override
    public void setOnEventListener(final OnEventListener listener) {
        mOnEventListener = listener;
    }

    @Override
    public void onInit(final UVCCamera uvcCamera, final Parameter parameter) {
    }

    @Override
    public void onReceivedFrame(final Frame frame) {
        try {
            drawJpegFrame(frame);
        } catch (Throwable t) {
            // ignore.
        } finally {
            frame.release();
        }
    }

    @Override
    public void onRelease() {
    }

    /**
     * JPEGのフレームデータを Surface に描画します.
     *
     * @param frame フレームデータ
     */
    private void drawJpegFrame(final Frame frame) {
        Canvas canvas = mSurface.lockCanvas(null);
        if (canvas == null) {
            postError(new RuntimeException("Failed to get a canvas from surface."));
            return;
        }

        // TODO canvas に収まるようにリサイズするべきか？

        try {
            byte[] buffer = frame.getBuffer();
            int length = frame.getLength();
            Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, length);
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, 0, 0, null);
                bitmap.recycle();
            }
        } finally {
            mSurface.unlockCanvasAndPost(canvas);
        }
    }

    /**
     * エラーを通知します.
     *
     * @param e 通知する例外
     */
    private void postError(final Exception e) {
        if (mOnEventListener != null) {
            mOnEventListener.onError(e);
        }
    }
}
