/*
 CertificateRequestCallback.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.ssl;


import java.security.cert.Certificate;

/**
 * 証明書署名要求処理のコールバック.
 *
 * @author NTT DOCOMO, INC.
 */
interface CertificateRequestCallback {

    /**
     * 成功コールバック.
     *
     * @param certificate ローカル認証局から発行された証明書
     * @param rootCertificate ルート証明書
     */
    void onCreate(Certificate certificate,
                  Certificate rootCertificate);

    /**
     * エラーコールバック.
     */
    void onError();

}
