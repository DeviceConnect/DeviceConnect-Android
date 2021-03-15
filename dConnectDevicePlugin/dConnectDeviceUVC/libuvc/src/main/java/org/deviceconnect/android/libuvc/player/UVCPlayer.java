/*
 UVCPlayer.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libuvc.player;

import android.view.Surface;

import org.deviceconnect.android.libuvc.Parameter;
import org.deviceconnect.android.libuvc.UVCCamera;
import org.deviceconnect.android.libuvc.UVCCameraException;
import org.deviceconnect.android.libuvc.decoder.UVCDecoder;
import org.deviceconnect.android.libuvc.decoder.UVCDecoderFactory;

import java.io.IOException;

/**
 * UVCCameraから送られてくるデータをSurfaceに描画を行うクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class UVCPlayer {
    /**
     * 映像を取得するUVCカメラ.
     */
    private UVCCamera mUVCCamera;

    /**
     * 映像を表示するサーフェイス.
     */
    private Surface mSurface;

    /**
     * UVC からの映像をデコードするクラス.
     */
    private UVCDecoder mUVCDecoder;

    /**
     * UVCPlayerのイベントを通知するリスナー.
     */
    private OnEventListener mOnEventListener;

    /**
     * 表示先のSurfaceを設定します.
     *
     * @param surface surface
     */
    public void setSurface(final Surface surface) {
        mSurface = surface;
    }

    /**
     * Surfaceに描画を開始します.
     *
     * @param uvcCamera UVCカメラ
     * @param parameter 映像を再生するパラメータ
     * @throws IOException 再生開始に失敗した場合に発生
     * @throws IllegalArgumentException uvcCamera, parameterがnullの場合に発生
     * @throws IllegalStateException UVCカメラが既に動作している場合に発生
     */
    public void start(final UVCCamera uvcCamera, final Parameter parameter) throws IOException {
        if (uvcCamera == null) {
            throw new IllegalArgumentException("uvcCamera is null.");
        }

        if (parameter == null) {
            throw new IllegalArgumentException("parameter is null.");
        }

        if (uvcCamera.isRunning()) {
            throw new IllegalStateException("UVCCamera is already running.");
        }

        if (mSurface == null) {
            throw new IOException("Surface is not set.");
        }

        mUVCCamera = uvcCamera;

        mUVCDecoder = UVCDecoderFactory.create(parameter);
        mUVCDecoder.setSurface(mSurface);
        mUVCDecoder.setOnEventListener(e -> postError(new UVCPlayerException(e)));
        mUVCDecoder.onInit(uvcCamera, parameter);

        mUVCCamera.setOnEventListener(new UVCCamera.OnEventListener() {
            @Override
            public void onStart() {
                postStarted();
            }

            @Override
            public void onStop() {
                postStopped();
            }

            @Override
            public void onError(final UVCCameraException e) {
                postError(new UVCPlayerException(e));
            }
        });
        mUVCCamera.setPreviewCallback(frame -> mUVCDecoder.onReceivedFrame(frame));
        mUVCCamera.startVideo(parameter);
    }

    /**
     * 再生を停止します.
     */
    public void stop() {
        try {
            mUVCCamera.stopVideo();
        } catch (IOException e) {
            // ignore.
        }

        try {
            mUVCDecoder.onRelease();
        } catch (Exception e) {
            // ignore.
        }
    }

    /**
     * UVCPlayerで発生したイベントを通知するリスナーを設定します.
     * @param listener リスナー
     */
    public void setOnEventListener(final OnEventListener listener) {
        mOnEventListener = listener;
    }

    /**
     * 開始イベントをリスナーに通知します.
     */
    private void postStarted() {
        if (mOnEventListener != null) {
            mOnEventListener.onStarted();
        }
    }

    /**
     * 停止イベントをリスナーに通知します.
     */
    private void postStopped() {
        if (mOnEventListener != null) {
            mOnEventListener.onStopped();
        }
    }

    /**
     * エラーイベントをリスナーに通知します.
     *
     * @param e エラー
     */
    private void postError(final UVCPlayerException e) {
        if (mOnEventListener != null) {
            mOnEventListener.onError(e);
        }
    }

    /**
     * UVCPlayerで発生したイベントを通知するためのリスナー.
     */
    public interface OnEventListener {
        /**
         * UVCPlayerの再生が開始されたことを通知します.
         */
        void onStarted();

        /**
         * UVCPlayerの再生が停止されたことを通知します.
         */
        void onStopped();

        /**
         * UVCPlayerでエラーが発生したことを通知します.
         *
         * @param e エラー原因の例外
         */
        void onError(UVCPlayerException e);
    }
}
