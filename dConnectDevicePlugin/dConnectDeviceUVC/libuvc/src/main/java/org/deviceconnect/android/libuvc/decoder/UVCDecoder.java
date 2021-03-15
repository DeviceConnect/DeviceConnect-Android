/*
 UVCDecoder.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libuvc.decoder;

import android.view.Surface;

import org.deviceconnect.android.libuvc.Frame;
import org.deviceconnect.android.libuvc.Parameter;
import org.deviceconnect.android.libuvc.UVCCamera;

/**
 * UVCからのフレームデータをデコードするクラスのインターフェース.
 *
 * @author NTT DOCOMO, INC.
 */
public interface UVCDecoder {
    /**
     * デコード先のSurfaceを設定します.
     * @param surface Surface
     */
    void setSurface(Surface surface);

    /**
     * デコーダで発生したイベントを通知するリスナーを設定します.
     * @param listener リスナー
     */
    void setOnEventListener(OnEventListener listener);

    /**
     * デコーダを初期化します.
     *
     * @param uvcCamera UVCカメラ
     * @param parameter UVCカメラに設定されているパラメータ
     */
    void onInit(UVCCamera uvcCamera, Parameter parameter);

    /**
     * UVCカメラから送られてくるフレームデータを受け取ります.
     *
     * @param frame フレームデータ
     */
    void onReceivedFrame(Frame frame);

    /**
     * デコーダの後始末を行います.
     */
    void onRelease();

    /**
     * デコーダのイベントを通知するリスナー.
     */
    interface OnEventListener {
        /**
         * デコーダでエラーが発生したことを通知します.
         *
         * @param e エラー原因
         */
        void onError(Exception e);
    }
}
