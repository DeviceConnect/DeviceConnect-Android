/*
 KeyStoreError.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.ssl;

/**
 * キーストア操作についてのエラーの定義.
 *
 * @author NTT DOCOMO, INC.
 */
public enum KeyStoreError {

    /**
     * キーストアから証明書を取得しようとした際、データ不整合のために取得できなかった場合のエラー.
     */
    BROKEN_KEYSTORE,

    /**
     * キーストアを永続化しようとした際、指定した形式がその実行環境では非サポートだった場合のエラー.
     */
    UNSUPPORTED_KEYSTORE_FORMAT,

    /**
     * 証明書を新規作成しようとした際、指定した形式がその実行環境では非サポートだった場合のエラー.
     */
    UNSUPPORTED_CERTIFICATE_FORMAT,

    /**
     * 不明な理由によりキーストアの永続化に失敗した場合のエラー.
     */
    FAILED_BACKUP_KEYSTORE

}
