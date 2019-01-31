/*
 CameraWrapperException.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.camera;

/**
 * カメラ操作時に発生する例外.
 *
 * @author NTT DOCOMO, INC.
 */
public class CameraWrapperException extends Exception {

    /**
     * コンストラクタ.
     * @param message エラーメッセージ
     */
    public CameraWrapperException(final String message) {
        super(message);
    }

    /**
     * コンストラクタ.
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public CameraWrapperException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * コンストラクタ.
     * @param cause 原因となった例外
     */
    public CameraWrapperException(final Throwable cause) {
        super(cause);
    }
}
