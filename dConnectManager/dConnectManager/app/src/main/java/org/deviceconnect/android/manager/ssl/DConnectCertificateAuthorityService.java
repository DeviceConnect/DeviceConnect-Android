/*
 DConnectCertificateAuthorityService.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.ssl;

import org.deviceconnect.android.ssl.CertificateAuthorityService;


/**
 * ローカル認証局サービス.
 *
 * <p>
 * プラグインに対してAIDL経由でサーバー証明書を提供する.
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectCertificateAuthorityService extends CertificateAuthorityService {

    /**
     * ルート証明書のキーストアのファイル名.
     *
     * アプリ領域に保存する際に使用される.
     */
    public static final String KEYSTORE_NAME = "root.p12";

    /**
     * 証明書の発行者名.
     */
    public static final String ISSUER_NAME = "Device Connect Root CA";

    @Override
    protected String getIssuerName() {
        return ISSUER_NAME;
    }

    @Override
    protected String getKeyStoreFileName() {
        return KEYSTORE_NAME;
    }

}
