/*
 MessagingException.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.plugin;

/**
 * プラグインへのメッセージ送信不可を示すチェック例外.
 *
 * @author NTT DOCOMO, INC.
 */
public class MessagingException extends Exception {

    private final Reason mReason;

    /**
     * コンストラクタ.
     *
     * @param cause 原因となった例外のインスタンス
     * @param reason メッセージ送信不可の理由
     */
    MessagingException(final Throwable cause, final Reason reason) {
        super(cause);
        mReason = reason;
    }

    /**
     * コンストラクタ.
     *
     * @param reason メッセージ送信不可の理由
     */
    MessagingException(final Reason reason) {
        mReason = reason;
    }

    /**
     * メッセージ送信不可の理由を取得する.
     *
     * @return メッセージ送信不可の理由
     */
    public Reason getReason() {
        return mReason;
    }

    /**
     * メッセージ送信不可の理由.
     */
    public enum Reason {

        /**
         * プラグインが有効でない.
         */
        NOT_ENABLED,

        /**
         * プラグインとの連携が中断されている.
         */
        CONNECTION_SUSPENDED,

        /**
         * プラグインと接続されていない.
         */
        NOT_CONNECTED
    }
}
