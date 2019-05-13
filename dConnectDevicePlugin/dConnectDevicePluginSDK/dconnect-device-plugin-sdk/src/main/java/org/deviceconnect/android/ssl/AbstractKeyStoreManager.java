/*
 AbstractKeyStoreManager.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.ssl;


import android.content.Context;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.deviceconnect.android.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.x500.X500Principal;


/**
 * キーストア管理クラスの基底クラス.
 *
 * キーストアをアプリ領域に永続化する.
 *
 * @author NTT DOCOMO, INC.
 */
abstract class AbstractKeyStoreManager implements KeyStoreManager {

    /**
     * キーストアの形式指定.
     */
    private static final String KEYSTORE_TYPE = "PKCS12";

    /**
     * キーストアのパスワード.
     */
    private static final char[] KEYSTORE_PASSWORD = "0000".toCharArray();

    /**
     * コンテキスト.
     */
    final Context mContext;

    /**
     * キーストア.
     */
    final KeyStore mKeyStore;

    /**
     * キーストアのファイル名.
     */
    private final String mKeyStoreFilePath;

    /**
     * ロガー.
     */
    private final Logger mLogger = Logger.getLogger("LocalCA");

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param keyStorePath キーストアの保存先となるファイルパス
     */
    AbstractKeyStoreManager(final Context context, final String keyStorePath) {
        mContext = context;
        mKeyStoreFilePath = keyStorePath;
        try {
            mKeyStore = createKeyStore();
        } catch (GeneralSecurityException e) {
            // NOTE: PKCS12 は　API Level 1 からサポートされている. よって、ここには入らない.
            throw new IllegalStateException(KEYSTORE_TYPE + " is not supported.", e);
        }
        boolean isSavedKeyStore = isSavedKeyStore();

        if (BuildConfig.DEBUG) {
            mLogger.info("isSavedKeyStore: " + isSavedKeyStore);
        }

        if (isSavedKeyStore) {
            try {
                loadKeyStore();

                if (BuildConfig.DEBUG) {
                    mLogger.info("Loaded keystore: path = " + mKeyStoreFilePath);
                }
            } catch (Exception e) {
                mLogger.log(Level.SEVERE, "Failed to load keystore: path = " + mKeyStoreFilePath, e);
            }
        }
    }

    @Override
    public Certificate getCertificate(final String alias) {
        try {
            return mKeyStore.getCertificate(alias);
        } catch (KeyStoreException e) {
            return null;
        }
    }

    @Override
    public void exportKeyStore(final File outputFile) throws IOException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(outputFile);
            saveKeyStore(out);
        } catch (GeneralSecurityException e) {
            throw new IOException("Failed to export keystore.", e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private KeyStore createKeyStore() throws GeneralSecurityException {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        try {
            keyStore.load(null, KEYSTORE_PASSWORD);
        } catch (IOException e) {
            throw new GeneralSecurityException("Unable to create empty keyStore", e);
        }
        return keyStore;
    }

    PrivateKey getPrivateKey(final String alias) {
        try {
            KeyStore.Entry entry = mKeyStore.getEntry(alias, new KeyStore.PasswordProtection(KEYSTORE_PASSWORD));
            if (entry instanceof KeyStore.PrivateKeyEntry) {
                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) entry;
                return privateKeyEntry.getPrivateKey();
            }
        } catch (GeneralSecurityException e) {
            // NOP.
        }
        return null;
    }

    private boolean isSavedKeyStore() {
        File parentDir = mContext.getFilesDir();
        return new File(parentDir, mKeyStoreFilePath).exists();
    }

    private void loadKeyStore() throws IOException, NoSuchAlgorithmException, CertificateException {
        mKeyStore.load(mContext.openFileInput(mKeyStoreFilePath), KEYSTORE_PASSWORD);
    }

    void saveKeyStore() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        saveKeyStore(mContext.openFileOutput(mKeyStoreFilePath, Context.MODE_PRIVATE));
    }

    private void saveKeyStore(final OutputStream out) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        mKeyStore.store(out, KEYSTORE_PASSWORD);
    }

    private X509Certificate generateX509V3Certificate(final KeyPair keyPair,
                                                      final X500Principal subject,
                                                      final X500Principal issuer,
                                                      final Date notBefore,
                                                      final Date notAfter,
                                                      final BigInteger serialNumber,
                                                      final GeneralNames generalNames,
                                                      final boolean isCA) throws GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        X509V3CertificateGenerator generator = new X509V3CertificateGenerator();
        generator.setSerialNumber(serialNumber);
        generator.setIssuerDN(issuer);
        generator.setSubjectDN(subject);
        generator.setNotBefore(notBefore);
        generator.setNotAfter(notAfter);
        generator.setPublicKey(keyPair.getPublic());
        generator.setSignatureAlgorithm("SHA256WithRSAEncryption");
        generator.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(isCA));
        generator.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(160));
        generator.addExtension(X509Extensions.ExtendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));
        if (generalNames != null) {
            generator.addExtension(X509Extensions.SubjectAlternativeName, false, generalNames);
        }
        return generator.generateX509Certificate(keyPair.getPrivate(), "BC");
    }

    @Override
    public X509Certificate generateX509V3Certificate(final KeyPair keyPair,
                                                     final X500Principal subject,
                                                     final X500Principal issuer,
                                                     final GeneralNames generalNames,
                                                     final boolean isCA) throws GeneralSecurityException {
        Calendar var2 = Calendar.getInstance();
        var2.set(2009, 0, 1);
        Date var3 = new Date(var2.getTimeInMillis());
        var2.set(2099, 0, 1);
        Date var4 = new Date(var2.getTimeInMillis());
        BigInteger serialNumber = BigInteger.valueOf(Math.abs(System.currentTimeMillis()));
        return generateX509V3Certificate(keyPair, subject, issuer, var3, var4, serialNumber, generalNames, isCA);
    }
}
