/*
 TagConstants.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.services;

/**
 * タグで使用する定数の定義.
 *
 * @author NTT DOCOMO, INC.
 */
public interface TagConstants {
    /**
     * テキストのタイプを定義.
     */
    String TYPE_TEXT = "text";

    /**
     * URIのタイプを定義.
     */
    String TYPE_URI = "uri";

    /**
     * マイムタイプを定義.
     */
    String TYPE_MIME = "mime";

    /**
     * リクエストコード.
     */
    String EXTRA_REQUEST_CODE = "request_code";

    /**
     * 1回だけ.
     */
    String EXTRA_ONCE = "once";

    /**
     * タグに書き込むデータ.
     */
    String EXTRA_TAG_DATA = "tag_data";

    /**
     * タグのタイプ.
     */
    String EXTRA_TAG_TYPE = "tag_type";

    /**
     * タグのタイプ.
     */
    String EXTRA_LANGUAGE_CODE = "language_code";

    /**
     * マイムタイプ.
     */
    String EXTRA_MIME_TYPE = "mime_type";

    /**
     * マイムデータ.
     */
    String EXTRA_MIME_DATA = "mime_data";

    /**
     * 成功.
     */
    int RESULT_SUCCESS = 1;

    /**
     * 失敗.
     */
    int RESULT_FAILED = 0;

    /**
     * パーミッションがないために失敗.
     */
    int RESULT_NO_PERMISSION = -1;

    /**
     * サポートされていないために失敗.
     */
    int RESULT_NOT_SUPPORT = -2;

    /**
     * 機能が無効にされているために失敗.
     */
    int RESULT_DISABLED = -3;

    /**
     * フォーマットが不正なために失敗.
     */
    int RESULT_INVALID_FORMAT = -4;

    /**
     * 書き込み許可がないために失敗.
     */
    int RESULT_NOT_WRIATEBLE = -5;
}
