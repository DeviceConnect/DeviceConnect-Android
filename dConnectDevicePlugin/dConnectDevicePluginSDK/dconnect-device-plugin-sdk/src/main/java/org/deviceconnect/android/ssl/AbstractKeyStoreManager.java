/*
 AbstractKeyStoreManager.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.ssl;


import android.content.Context;
import android.util.Log;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.deviceconnect.android.BuildConfig;

import java.io.ByteArrayInputStream;
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
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
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
     * ロガー.
     */
    private final Logger mLogger = Logger.getLogger("LocalCA");

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
     * キーストアのパスワード.
     */
    private final String mKeyStorePassword;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param keyStorePath キーストアの保存先となるファイルパス
     * @param keyStorePassword パスワード
     */
    AbstractKeyStoreManager(final Context context, final String keyStorePath, final String keyStorePassword) {
        mContext = context;
        mKeyStoreFilePath = keyStorePath;
        mKeyStorePassword = keyStorePassword;
        try {
            mKeyStore = createKeyStore();
        } catch (GeneralSecurityException e) {
            // NOTE: PKCS12 は API Level 1 からサポートされている. よって、ここには入らない.
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
            keyStore.load(null, mKeyStorePassword.toCharArray());
        } catch (IOException e) {
            throw new GeneralSecurityException("Unable to create empty keyStore", e);
        }
        return keyStore;
    }

    PrivateKey getPrivateKey(final String alias) {
        try {
            KeyStore.Entry entry = mKeyStore.getEntry(alias, new KeyStore.PasswordProtection(mKeyStorePassword.toCharArray()));
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
        mKeyStore.load(mContext.openFileInput(mKeyStoreFilePath), mKeyStorePassword.toCharArray());
    }

    void saveKeyStore() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        saveKeyStore(mContext.openFileOutput(mKeyStoreFilePath, Context.MODE_PRIVATE));
    }

    private void saveKeyStore(final OutputStream out) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        mKeyStore.store(out, mKeyStorePassword.toCharArray());
    }

    public boolean clean() throws KeyStoreException, IOException {
        for (Enumeration<String> e = mKeyStore.aliases(); e.hasMoreElements(); ) {
            String alias = e.nextElement();
            mKeyStore.deleteEntry(alias);
        }
        return mContext.deleteFile(mKeyStoreFilePath);
    }

    private X509Certificate buildX509V3Certificate(final KeyPair keyPair,
                                                   final String issuerName,
                                                   final String subjectName,
                                                   final Date notBefore,
                                                   final Date notAfter,
                                                   final BigInteger serialNumber,
                                                   final GeneralNames generalNames,
                                                   final boolean isCA) throws GeneralSecurityException, IOException, OperatorCreationException {
        Log.d("Certificate", "***** buildX509V3Certificate: SANs = " + generalNames);

        AsymmetricKeyParameter privateKey = PrivateKeyFactory.createKey(keyPair.getPrivate().getEncoded());
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256WithRSAEncryption");
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
        ContentSigner signer = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(privateKey);

        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
                new X500Name(issuerName),
                serialNumber,
                notBefore,
                notAfter,
                new X500Name(subjectName),
                SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded())
        );
        builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(isCA));
        int keyUsage = isCA ? (KeyUsage.cRLSign | KeyUsage.keyCertSign)
                : (KeyUsage.keyEncipherment | KeyUsage.digitalSignature);
        builder.addExtension(Extension.keyUsage, true, new KeyUsage(keyUsage));
        if (!isCA) {
            builder.addExtension(Extension.extendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));
        }
        if (generalNames != null) {
            builder.addExtension(Extension.subjectAlternativeName, false, generalNames);
        }
        X509CertificateHolder holder = builder.build(signer);
        byte[] encoded = holder.getEncoded();
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream in = new ByteArrayInputStream(encoded);
        return (X509Certificate) factory.generateCertificate(in);
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
        try {
            return buildX509V3Certificate(keyPair, subject.getName(), issuer.getName(), var3, var4, serialNumber, generalNames, isCA);
        } catch (Throwable e) {
            throw new GeneralSecurityException(e);
        }
    }
}
