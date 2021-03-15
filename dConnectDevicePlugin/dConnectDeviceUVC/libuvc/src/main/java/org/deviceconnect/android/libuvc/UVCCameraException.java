/*
 UVCCameraException.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libuvc;

/**
 * UVCCameraで発生するエラーの例外クラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class UVCCameraException extends RuntimeException {

    /**
     * コンストラクタ.
     */
    UVCCameraException() {}

    /**
     * コンストラクタ.
     *
     * @param e 例外
     */
    UVCCameraException(final Throwable e) {
        super(e);
    }

    /**
     * コンストラクタ.
     *
     * @param message エラーメッセージ
     */
    UVCCameraException(final String message) {
        super(message);
    }
}
