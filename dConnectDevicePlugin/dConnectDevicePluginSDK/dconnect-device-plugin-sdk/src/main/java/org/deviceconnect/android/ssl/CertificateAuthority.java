package org.deviceconnect.android.ssl;


import android.content.Context;

import org.bouncycastle.jce.PKCS10CertificationRequest;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ローカル証明書認証局.
 */
class CertificateAuthority {

    /**
     * ルート証明書のキーストア.
     */
    private final RootKeyStoreManager mRootKeyStoreMgr;

    private final Logger mLogger = Logger.getLogger("LocalCA");

    CertificateAuthority(final Context context,
                                final String keyStorePath) {
        mRootKeyStoreMgr = new RootKeyStoreManager(context, keyStorePath);
    }

    byte[] requestCertificate(final byte[] pkcs10) {
        final Certificate[] certificate = new Certificate[1];
        final KeyStoreError[] errors = new KeyStoreError[1];
        final CountDownLatch lock = new CountDownLatch(1);

        // キーストア取得
        mRootKeyStoreMgr.requestKeyStore(new KeyStoreCallback() {
            @Override
            public void onSuccess(final KeyStore keyStore) {
                try {
                    // 証明書要求を解析
                    PKCS10CertificationRequest request = new PKCS10CertificationRequest(pkcs10);

                    PrivateKey signingKey = mRootKeyStoreMgr.getPrivateKey(RootKeyStoreManager.ALIAS);
                    KeyPair keyPair = new KeyPair(request.getPublicKey(), signingKey);
                    certificate[0] = mRootKeyStoreMgr.generateX509V3Certificate(keyPair, "CN=localhost");
                } catch (Exception e) {
                    // NOP.
                    mLogger.log(Level.SEVERE, "Failed to generate keystore: ", e);
                } finally {
                    // スレッドブロックを解除
                    lock.countDown();
                }
            }

            @Override
            public void onError(final KeyStoreError error) {
                mLogger.severe("Failed to get keystore: " + error);
                errors[0] = error;

                // スレッドブロックを解除
                lock.countDown();
            }
        });

        try {
            lock.await(10, TimeUnit.SECONDS);
            if (certificate[0] == null && errors[0] == null) {
                mLogger.log(Level.SEVERE, "Timeout was occurred for keystore generation.");
            }
            Certificate result = certificate[0];
            if (result != null) {
                return result.getEncoded();
            }
        } catch (InterruptedException e) {
            mLogger.log(Level.SEVERE, "Failed to encode certificate to byte array: ", e);
        } catch (CertificateEncodingException e) {
            mLogger.log(Level.SEVERE, "Failed to encode certificate to byte array: ", e);
        }
        return null;
    }

}
