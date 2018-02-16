package org.deviceconnect.android.ssl;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/**
 * サーバ証明書用のキーストアを提供するインターフェース.
 */
public interface KeyStoreManager {

    /**
     * キーストアから証明書を取得する.
     *
     * @return 証明書のインスタンス. 未生成の場合は<code>null</code>
     */
    Certificate getCertificate();

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

    /**
     * キーストアを外部ファイルに出力する.
     *
     * @param outputFile 出力先となるファイル
     * @throws IOException 出力に失敗した場合
     */
    void exportKeyStore(final File outputFile) throws IOException;

    X509Certificate generateX509V3Certificate(KeyPair keyPair, String commonName) throws GeneralSecurityException;
}
