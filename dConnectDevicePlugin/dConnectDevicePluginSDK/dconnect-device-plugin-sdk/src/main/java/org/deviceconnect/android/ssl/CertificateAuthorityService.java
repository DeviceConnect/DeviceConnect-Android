package org.deviceconnect.android.ssl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;


/**
 * ローカル認証局サービス.
 *
 * DeviceConnectプラグインに対して証明書要求機能を提供する.
 */
public abstract class CertificateAuthorityService extends Service {

    private CertificateAuthority mLocalCA;

    @Override
    public void onCreate() {
        super.onCreate();
        mLocalCA = new CertificateAuthority(getApplicationContext(), getIssuerName(), getKeyStoreFileName());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ICertificateAuthority.Stub() {
            @Override
            public byte[] requestCertificate(final byte[] certificateRequest) throws RemoteException {
                // NOTE: 将来的に必要になったら、ここで呼び出し元によって証明書署名要求の可否を判定.
                return mLocalCA.requestCertificate(certificateRequest);
            }

            @Override
            public byte[] getRootCertificate() throws RemoteException {
                return mLocalCA.getRootCertificate();
            }
        };
    }

    protected String getIssuerName() {
        return "Device Connect Root CA";
    }

    protected String getKeyStoreFileName() {
        return "keystore.p12";
    }
}
