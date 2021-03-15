/*
 UVCUncompressedDecoder.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libuvc.decoder;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.Surface;

import org.deviceconnect.android.libuvc.Frame;
import org.deviceconnect.android.libuvc.ImageUtils;
import org.deviceconnect.android.libuvc.Parameter;
import org.deviceconnect.android.libuvc.UVCCamera;

/**
 * 無圧縮の画像をSurfaceに描画するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
class UVCUncompressedDecoder implements UVCDecoder {
    /**
     * UVCに渡すパラメータ.
     */
    private Parameter mParameter;

    /**
     * UVCからの映像を格納するBitmap.
     */
    private Bitmap mBitmap;

    /**
     * 描画を行うSurface.
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
        mParameter = parameter;
        mBitmap = Bitmap.createBitmap(mParameter.getWidth(), mParameter.getHeight(), Bitmap.Config.ARGB_8888);
    }

    @Override
    public void onReceivedFrame(final Frame frame) {
        try {
            drawFrame(frame);
        } finally {
            frame.release();
        }
    }

    @Override
    public void onRelease() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    /**
     * 非圧縮の画像データを Surface に描画します.
     *
     * @param frame フレームバッファ
     */
    private void drawFrame(final Frame frame) {
        Canvas canvas = mSurface.lockCanvas(null);
        if (canvas == null) {
            postError(new RuntimeException("Failed to get a canvas from surface."));
            return;
        }

        try {
            if (mBitmap == null) {
                return;
            }

            boolean result = false;
            Long guid = mParameter.getExtra("guid");
            if (guid == null || guid == 0) {
                result = ImageUtils.decodeYUY2(mBitmap, frame.getBuffer(), mParameter.getWidth(), mParameter.getHeight());
            } else if (guid == 1) {
                result = ImageUtils.decodeNV12(mBitmap, frame.getBuffer(), mParameter.getWidth(), mParameter.getHeight());
            } else if (guid == 2) {
                result = ImageUtils.decodeM420(mBitmap, frame.getBuffer(), mParameter.getWidth(), mParameter.getHeight());
            } else if (guid == 3) {
                result = ImageUtils.decodeI420(mBitmap, frame.getBuffer(), mParameter.getWidth(), mParameter.getHeight());
            }

            if (result) {
                canvas.drawBitmap(mBitmap, 0, 0, null);
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
