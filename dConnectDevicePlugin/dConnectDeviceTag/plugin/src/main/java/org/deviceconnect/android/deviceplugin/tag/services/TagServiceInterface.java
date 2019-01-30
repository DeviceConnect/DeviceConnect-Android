/*
 TagServiceInterface.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.services;

/**
 * タグを読み込むためのインターフェースクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public interface TagServiceInterface {
    /**
     * タグを読み込むためのActivityをコントロールするためのインターフェースを設定します.
     * <p>
     * null が指定された場合には、設定を削除します。
     * </p>
     * @param controller コントローラー
     */
    void setTagController(TagController controller);

    /**
     * タグを読み込んだ結果を通知します.
     *
     * @param requestCode リクエストコード
     * @param result タグ読み込み結果
     * @param tagInfo タグの情報
     */
    void onTagReaderActivityResult(String requestCode, int result, TagInfo tagInfo);

    /**
     * タグを書き込み結果を通知します.
     *
     * @param requestCode リクエストコード
     * @param result 書き込みの結果
     */
    void onTagWriterActivityResult(String requestCode, int result);

    /**
     * タグを読み込むためのActivityを操作するコントローラ.
     */
    interface TagController {
        /**
         * Activityを終了します.
         */
        void finishActivity();
    }

    /**
     * タグ読み込みの結果を通知するコールバック.
     */
    interface ReaderCallback {
        /**
         * タグ読み込みの結果を通知します.
         *
         * @param result NFC書き込みの結果
         * @param tagInfo タグ情報のリスト
         */
        void onResult(int result, TagInfo tagInfo);
    }

    /**
     * タグ書き込みの結果を通知するコールバック.
     */
    interface WriterCallback {
        /**
         * タグ書き込みの結果を通知します.
         *
         * @param result NFC書き込みの結果
         */
        void onResult(int result);
    }
}
