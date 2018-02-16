package org.deviceconnect.android.manager.ssl;

import org.deviceconnect.android.ssl.CertificateAuthorityService;


/**
 * ローカル認証局サービス.
 */
public class DConnectCertificateAuthorityService extends CertificateAuthorityService {

    public static final String KEYSTORE_NAME = "root.p12";

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
