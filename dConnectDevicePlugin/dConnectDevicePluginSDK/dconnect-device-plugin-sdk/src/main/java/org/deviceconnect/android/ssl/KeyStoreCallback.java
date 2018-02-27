/*
 KeyStoreCallback.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.ssl;


import java.security.KeyStore;

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
     */
    void onSuccess(KeyStore keyStore);

    /**
     * 失敗コールバック.
     *
     * @param error エラー
     */
    void onError(KeyStoreError error);

}
