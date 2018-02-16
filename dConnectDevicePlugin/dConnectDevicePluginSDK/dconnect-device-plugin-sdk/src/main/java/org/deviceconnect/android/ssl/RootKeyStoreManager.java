package org.deviceconnect.android.ssl;


import android.content.Context;

import com.google.fix.PRNGFixes;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.x500.X500Principal;

class RootKeyStoreManager extends AbstractKeyStoreManager implements KeyStoreManager {

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final Logger mLogger = Logger.getLogger("LocalCA");

    private final String mSubjectName;

    RootKeyStoreManager(final Context context,
                        final String subjectName,
                        final String keyStorePath) {
        super(context, keyStorePath);
        mSubjectName = subjectName;

        // Java Cryptography Architectureの乱数種に関するセキュリティ問題への対処.
        PRNGFixes.apply();
    }

    @Override
    public void requestKeyStore(final String ipAddress, final KeyStoreCallback callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Certificate cert = mKeyStore.getCertificate(ipAddress);
                    if (cert != null) {
                        callback.onSuccess(mKeyStore);
                        return;
                    }
                    mLogger.info("Generating self-signed server certificate...");
                    generateSelfSignedCertificate();
                    mLogger.info("Generated self-signed server certificate...");
                } catch (KeyStoreException e) {
                    mLogger.log(Level.SEVERE, "Failed to generate self-signed server certificate.", e);
                    callback.onError(KeyStoreError.BROKEN_KEYSTORE);
                } catch (GeneralSecurityException e) {
                    mLogger.log(Level.SEVERE, "Failed to generate self-signed server certificate.", e);
                    callback.onError(KeyStoreError.UNSUPPORTED_KEYSTORE_FORMAT);
                }

                try {
                    saveKeyStore();
                    callback.onSuccess(mKeyStore);
                } catch (Exception e) {
                    mLogger.log(Level.SEVERE, "Failed to save self-signed server certificate.", e);
                    callback.onError(KeyStoreError.FAILED_BACKUP_KEYSTORE);
                }
            }
        });
    }

    private void generateSelfSignedCertificate() throws GeneralSecurityException {
        KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = kg.generateKeyPair();
        X500Principal subject = new X500Principal("CN=" + mSubjectName);
        Certificate cert = generateX509V3Certificate(keyPair, subject, subject, null,true);
        Certificate[] chain = {cert};
        mKeyStore.setKeyEntry(mSubjectName, keyPair.getPrivate(), null, chain);
    }
}
