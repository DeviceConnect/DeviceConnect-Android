/*
 ConnectingException.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.plugin;


/**
 * プラグインとの接続に失敗したことを示すチェック例外.
 *
 * @author NTT DOCOMO, INC.
 */
class ConnectingException extends Exception {

    /**
     * コンストラクタ.
     *
     * @param cause 原因となった例外のインスタンス
     */
    ConnectingException(final Throwable cause) {
        super(cause);
    }

    /**
     * コンストラクタ.
     *
     * @param message エラーメッセージ
     */
    ConnectingException(final String message) {
        super(message);
    }

}
