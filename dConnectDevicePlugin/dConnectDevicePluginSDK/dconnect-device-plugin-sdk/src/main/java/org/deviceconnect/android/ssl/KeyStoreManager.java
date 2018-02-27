/*
 KeyStoreManager.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.ssl;

import org.bouncycastle.asn1.x509.GeneralNames;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

/**
 * サーバ証明書用のキーストアを提供するインターフェース.
 *
 * @author NTT DOCOMO, INC.
 */
public interface KeyStoreManager {

    /**
     * キーストアから証明書を取得する.
     *
     * @param alias エイリアス
     * @return 証明書のインスタンス. 未生成の場合は<code>null</code>
     */
    Certificate getCertificate(String alias);

    /**
     * キーストアを非同期で取得する.
     *
     * 取得したキーストアにはサーバ証明書がすでに格納されている.
     *
     * 初回実行時は、サーバ証明書が未生成であるため、ローカル認証局に対する証明書要求を実行する.
     *
     * @param ipAddress IPv4アドレス
     * @param callback コールバック
     */
    void requestKeyStore(String ipAddress, KeyStoreCallback callback);

    /**
     * キーストアを外部ファイルに出力する.
     *
     * ルートCA証明書も同梱する.
     *
     * @param outputFile 出力先となるファイル
     * @throws IOException 出力に失敗した場合
     */
    void exportKeyStore(final File outputFile) throws IOException;

    /**
     * X.509証明書を生成する.
     *
     * @param keyPair キーペア
     * @param subject サプジェクト名
     * @param issuer 発行者名
     * @param generalNames SANs
     * @param isCA 認証局の証明書を発行する場合は<code>true</code>、それ以外の場合は<code>false</code>
     * @return X.509証明書
     * @throws GeneralSecurityException 生成に失敗した場合
     */
    X509Certificate generateX509V3Certificate(KeyPair keyPair,
                                              X500Principal subject,
                                              X500Principal issuer,
                                              GeneralNames generalNames,
                                              boolean isCA) throws GeneralSecurityException;
}
