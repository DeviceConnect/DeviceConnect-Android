/*
 KeyStoreCallback.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.ssl;


import java.security.KeyStore;
import java.security.cert.Certificate;

/**
 * キーストア要求コールバック.
 *
 * @author NTT DOCOMO, INC.
 */
public interface KeyStoreCallback {

    /**
     * 成功コールバック.
     *
     * @param keyStore キーストア
     * @param cert サーバ証明書
     * @param rootCert ルート証明書
     */
    void onSuccess(KeyStore keyStore, Certificate cert, Certificate rootCert);

    /**
     * 失敗コールバック.
     *
     * @param error エラー
     */
    void onError(KeyStoreError error);

}
