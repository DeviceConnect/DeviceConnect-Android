/*
 UVCPlayerException.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libuvc.player;

/**
 * UVCPlayerで発生したエラーの例外クラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class UVCPlayerException extends RuntimeException {

    /**
     * コンストラクタ.
     * @param e 例外
     */
    UVCPlayerException(final Exception e) {
        super(e);
    }

    /**
     * コンストラクタ.
     * @param message エラーメッセージ
     */
    UVCPlayerException(final String message) {
        super(message);
    }
}
