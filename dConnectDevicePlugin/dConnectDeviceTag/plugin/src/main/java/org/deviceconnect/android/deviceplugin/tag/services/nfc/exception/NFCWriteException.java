/*
 NFCWriteException.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.services.nfc.exception;

/**
 * NFC タグにデータを書き込む場合に発生する例外.
 *
 * @author NTT DOCOMO, INC.
 */
public class NFCWriteException extends Exception {

    /**
     * 書き込み失敗時のエラーコード.
     */
    public enum ErrorCode {
        /**
         * IOエラーが発生した場合のエラーコード.
         */
        IO_ERROR,

        /**
         * NFC のフォーマット不正が発生した場合のエラーコード.
         */
        INVALID_FORMAT,

        /**
         * NFC に書き込み禁止の場合のエラーコード.
         */
        NOT_WRITABLE,
    }

    /**
     * エラーコード.
     */
    private ErrorCode mCode;

    /**
     * コンストラクタ.
     * @param code エラーコード
     */
    public NFCWriteException(final ErrorCode code) {
        super();
        mCode = code;
    }

    /**
     * コンストラクタ.
     * @param code エラーコード
     * @param message エラーメッセージ
     */
    public NFCWriteException(final ErrorCode code, final String message) {
        super(message);
        mCode = code;
    }

    /**
     * コンストラクタ.
     * @param code エラーコード
     * @param message エラーメッセージ
     * @param e エラー原因になった例外
     */
    public NFCWriteException(final ErrorCode code, final String message, final Throwable e) {
        super(message, e);
        mCode = code;
    }

    /**
     * エラーコードを取得します.
     *
     * @return エラーコード
     */
    public ErrorCode getCode() {
        return mCode;
    }
}
