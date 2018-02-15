package org.deviceconnect.android.ssl;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

/**
 * サーバ証明書用のキーストアを提供するインターフェース.
 */
public interface KeyStoreManager {

    /**
     * キーストアを非同期で取得する.
     *
     * 取得したキーストアにはサーバ証明書がすでに格納されている.
     *
     * 初回実行時は、サーバ証明書が未生成であるため、ローカル認証局に対する証明書要求を実行する.
     *
     * @param callback コールバック
     */
    void requestKeyStore(KeyStoreCallback callback);

    X509Certificate generateX509V3Certificate(final KeyPair keyPair, final String commonName) throws GeneralSecurityException;
}
