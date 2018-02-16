package org.deviceconnect.android.ssl;


import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.google.fix.PRNGFixes;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

class RootKeyStoreManager extends AbstractKeyStoreManager implements KeyStoreManager {

    static final String ALIAS = "RootCA";

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final Logger mLogger = Logger.getLogger("LocalCA");

    RootKeyStoreManager(final Context context,
                        final String keyStorePath) {
        super(context, keyStorePath);

        // Java Cryptography Architectureの乱数種に関するセキュリティ問題への対処.
        PRNGFixes.apply();
    }

    @Override
    protected String getDefaultAlias() {
        return ALIAS;
    }

    @Override
    public void requestKeyStore(final KeyStoreCallback callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Certificate cert = mKeyStore.getCertificate(ALIAS);
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
        String commonName = "CN=localhost"; //getCertificateName(getUniqueId());
        Certificate cert = generateX509V3Certificate(keyPair, commonName);
        Certificate[] chain = {cert};
        mKeyStore.setKeyEntry(ALIAS, keyPair.getPrivate(), null, chain);
    }

    /**
     * Get Unique ID.
     * @return Unique ID
     */
    private String getUniqueId() {
        String id = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        // null ANDROID_ID is possible on emulator
        return id != null ? id : "emulator";
    }

    /**
     * Returns the name that should be used in a new certificate.
     * <p>
     * The format is:
     * "CN=Device Connect-server/PRODUCT/DEVICE/MODEL/unique identifier"
     * @param id ID
     * @return Certificate Name
     */
    private static String getCertificateName(final String id) {
        return "CN=Device Connect-server/" + Build.PRODUCT + "/" + Build.DEVICE + "/" + Build.MODEL + "/" + id;
    }
}
