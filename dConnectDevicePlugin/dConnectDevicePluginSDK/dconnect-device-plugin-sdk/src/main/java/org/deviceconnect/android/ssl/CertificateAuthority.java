package org.deviceconnect.android.ssl;


import android.content.Context;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Name;
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

import javax.security.auth.x500.X500Principal;

/**
 * ローカル証明書認証局.
 */
class CertificateAuthority {

    /**
     * ルート証明書のキーストア.
     */
    private final RootKeyStoreManager mRootKeyStoreMgr;

    private final Logger mLogger = Logger.getLogger("LocalCA");

    private final String mIssuerName;

    CertificateAuthority(final Context context,
                         final String issuerName,
                         final String keyStorePath) {
        mRootKeyStoreMgr = new RootKeyStoreManager(context, issuerName, keyStorePath);
        mIssuerName = issuerName;
    }

    byte[] requestCertificate(final byte[] pkcs10) {
        final Certificate[] certificate = new Certificate[1];
        final KeyStoreError[] errors = new KeyStoreError[1];
        final CountDownLatch lock = new CountDownLatch(1);

        // キーストア取得
        mRootKeyStoreMgr.requestKeyStore(mIssuerName, new KeyStoreCallback() {
            @Override
            public void onSuccess(final KeyStore keyStore) {
                try {
                    // 証明書要求を解析
                    PKCS10CertificationRequest request = new PKCS10CertificationRequest(pkcs10);

                    final String ipAddress = "192.168.2.16"; // TODO 証明書要求から取得
                    PrivateKey signingKey = mRootKeyStoreMgr.getPrivateKey(mIssuerName);
                    KeyPair keyPair = new KeyPair(request.getPublicKey(), signingKey);
                    X500Principal subject = new X500Principal("CN=" + ipAddress);
                    X500Principal issuer = new X500Principal("CN=" + mIssuerName);
                    GeneralNames generalNames = new GeneralNames(new DERSequence(new ASN1Encodable[] {
                            new GeneralName(GeneralName.dNSName, "localhost"),
                            new GeneralName(GeneralName.iPAddress, "0.0.0.0"),
                            new GeneralName(GeneralName.iPAddress, "127.0.0.1"),
                            new GeneralName(GeneralName.iPAddress, ipAddress)
                    }));
                    certificate[0] = mRootKeyStoreMgr.generateX509V3Certificate(keyPair, subject, issuer, generalNames, false);
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
