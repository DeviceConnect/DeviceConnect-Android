/*
 EndPointKeyStoreManager.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.ssl;


import android.content.ComponentName;
import android.content.Context;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.deviceconnect.android.BuildConfig;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.x500.X500Principal;

/**
 * エンドポイント証明書用のキーストアを管理するクラス.
 *
 * <p>
 * キーストアを外部から要求された時、証明書が未生成だった場合は、
 * Device Connect Managerのローカル認証局に対して証明書要求を送信し、エンドポイント用サーバ証明書を取得する.
 *
 * 証明書の発行は、数秒かかる場合があるため、別スレッド上で処理される.
 *
 * NOTE: 本クラスの保持する証明書はただ1つ.
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
public class EndPointKeyStoreManager extends AbstractKeyStoreManager implements KeyStoreManager {

    /**
     * デフォルトのルート認証局.
     */
    private static final ComponentName DEFAULT_ROOT_CA = new ComponentName("org.deviceconnect.android.manager",
            "org.deviceconnect.android.manager.ssl.DConnectCertificateAuthorityService");

    /**
     * 証明書のエイリアス.
     */
    private final String mAlias;

    /**
     * 本オブジェクトからの証明書要求先のルート認証局.
     */
    private final ComponentName mRootCA;

    /**
     * 証明書発行スレッド.
     */
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    /**
     * ロガー.
     */
    private final Logger mLogger = Logger.getLogger("LocalCA");

    /**
     * 証明書にSANsとして記載した名前リスト.
     */
    private final List<SAN> mSANs = new ArrayList<>();

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param keyStorePath キーストアの保存先
     */
    public EndPointKeyStoreManager(final Context context,
                                   final String keyStorePath) {
        this(context, keyStorePath, context.getPackageName());
    }

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param keyStorePath キーストアの保存先
     */
    public EndPointKeyStoreManager(final Context context,
                                   final String keyStorePath,
                                   final String alias) {
        this(context, keyStorePath, alias, DEFAULT_ROOT_CA);
    }

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param keyStorePath キーストアの保存先
     * @param rootCA 証明書要求の送信先
     */
    EndPointKeyStoreManager(final Context context,
                            final String keyStorePath,
                            final String alias,
                            final ComponentName rootCA) {
        super(context, keyStorePath);
        mRootCA = rootCA;
        mAlias = alias;
        restoreIPAddress(alias);
    }

    /**
     * 保存していた証明書からIPアドレスを取り出す.
     *
     * @param alias 証明書のエイリアス
     */
    private void restoreIPAddress(final String alias) {
        if (BuildConfig.DEBUG) {
            mLogger.log(Level.INFO, "Checking IP Addresses...: alias = " + alias);
        }
        try {
            Certificate certificate = mKeyStore.getCertificate(alias);
            if (certificate == null) {
                if (BuildConfig.DEBUG) {
                    mLogger.info("Certificate is not stored yet: alias = " + alias);
                }
                return;
            }
            if (BuildConfig.DEBUG) {
                mLogger.log(Level.INFO, "Restoring IP Addresses...: alias = " + alias);
            }
            if (certificate instanceof X509Certificate) {
                X509Certificate x509 = (X509Certificate) certificate;
                Collection<List<?>> names = x509.getSubjectAlternativeNames();
                if (names != null) {
                    if (BuildConfig.DEBUG) {
                        mLogger.log(Level.INFO, "SANs: size = " + names.size());
                    }
                    for (Iterator<List<?>> it = names.iterator(); it.hasNext(); ) {
                        List<?> list = it.next();
                        if (list.size() == 2) {
                            Object tagNo = list.get(0);
                            Object value = list.get(1);

                            if (BuildConfig.DEBUG) {
                                mLogger.info("SAN: tagNo = " + tagNo + ", value = " + value);
                            }

                            if (tagNo instanceof Integer && value instanceof String) {
                                mSANs.add(new SAN((Integer) tagNo, (String) value));
                            }
                        }
                    }
                } else {
                    if (BuildConfig.DEBUG) {
                        mLogger.log(Level.INFO, "No SANs is defined in certificate.");
                    }
                }
            } else {
                mLogger.log(Level.SEVERE, "Certificate format is not X.509: class = " + certificate.getClass());
            }
        } catch (CertificateParsingException e) {
            mLogger.log(Level.WARNING, "Failed to parse IP Addresses of certificate.", e);
        } catch (KeyStoreException e) {
            mLogger.log(Level.WARNING, "Failed to restore IP Addresses.", e);
        }
    }

    /**
     * 指定したIPアドレスに対して、すでに証明書を発行済であるかどうかを返す.
     *
     * @param ipAddress IPアドレス
     * @return 発行済である場合は<code>true</code>、そうでない場合は<code>false</code>
     */
    private boolean hasIPAddress(final String ipAddress) {
        for (SAN address : mSANs) {
            if (address.mName.equals(ipAddress)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void requestKeyStore(final String ipAddress, final KeyStoreCallback callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG) {
                    mLogger.info("Requested keystore: alias = " + getAlias() + ", IP Address = " + ipAddress);
                }
                try {
                    String alias = getAlias();
                    if (hasIPAddress(ipAddress)) {
                        if (BuildConfig.DEBUG) {
                            mLogger.info("Certificate is cached for alias: " + alias);
                        }
                        Certificate[] chain = mKeyStore.getCertificateChain(getAlias());
                        callback.onSuccess(mKeyStore, chain[0], chain[1]);
                    } else {
                        if (BuildConfig.DEBUG) {
                            mLogger.info("Generating key pair...");
                        }
                        final KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
                        final KeyPair keyPair = keyGenerator.generateKeyPair();

                        if (BuildConfig.DEBUG) {
                            mLogger.info("Generated key pair.");
                            mLogger.info("Executing certificate request...");
                        }

                        final CertificateAuthorityClient localCA = new CertificateAuthorityClient(mContext, mRootCA);

                        final List<ASN1Encodable> names = new ArrayList<>();
                        names.add(new GeneralName(GeneralName.iPAddress, ipAddress));
                        for (SAN cache : mSANs) {
                            if (!cache.mName.equals(ipAddress)) {
                                names.add(new GeneralName(cache.mTagNo, cache.mName));
                            }
                        }
                        names.add(new GeneralName(GeneralName.iPAddress, "0.0.0.0"));
                        names.add(new GeneralName(GeneralName.iPAddress, "127.0.0.1"));
                        names.add(new GeneralName(GeneralName.dNSName, "localhost"));
                        GeneralNames generalNames = new GeneralNames(new DERSequence(names.toArray(new ASN1Encodable[names.size()])));

                        localCA.executeCertificateRequest(createCSR(keyPair, "localhost", generalNames), new CertificateRequestCallback() {
                            @Override
                            public void onCreate(final Certificate cert, final Certificate rootCert) {
                                if (BuildConfig.DEBUG) {
                                    mLogger.info("Generated server certificate");
                                }

                                try {
                                    Certificate[] chain = {cert, rootCert};
                                    setCertificate(chain, keyPair.getPrivate());
                                    saveKeyStore();
                                    if (BuildConfig.DEBUG) {
                                        mLogger.info("Saved server certificate");
                                    }
                                    mSANs.add(new SAN(GeneralName.iPAddress, ipAddress));
                                    callback.onSuccess(mKeyStore, cert, rootCert);
                                } catch (Exception e) {
                                    mLogger.log(Level.SEVERE, "Failed to save server certificate", e);
                                    callback.onError(KeyStoreError.FAILED_BACKUP_KEYSTORE);
                                } finally {
                                    localCA.dispose();
                                }
                            }

                            @Override
                            public void onError() {
                                mLogger.severe("Failed to generate server certificate");

                                localCA.dispose();
                                callback.onError(KeyStoreError.FAILED_BACKUP_KEYSTORE);
                            }
                        });
                    }
                } catch (KeyStoreException e) {
                    callback.onError(KeyStoreError.BROKEN_KEYSTORE);
                } catch (GeneralSecurityException e) {
                    callback.onError(KeyStoreError.UNSUPPORTED_CERTIFICATE_FORMAT);
                }
            }
        });
    }

    /**
     * 証明書のエイリアスを返す.
     *
     * @return エイリアス
     */
    private String getAlias() {
        return mAlias;
    }

    /**
     * キーストアに証明書チェーンとプライベートキーのペアを保存する.
     *
     * @param certChain 証明書チェーン
     * @param privateKey プライベートキー
     * @throws KeyStoreException 保存に失敗した場合
     */
    private void setCertificate(final Certificate[] certChain, final PrivateKey privateKey) throws KeyStoreException {
        mKeyStore.setKeyEntry(getAlias(), privateKey, null, certChain);
    }

    /**
     * 証明書署名要求のオブジェクトを作成する.
     *
     * @param keyPair キーペア
     * @param commonName コモンネーム
     * @param generalNames SANs
     * @return 証明書署名要求のオブジェクト
     * @throws GeneralSecurityException 作成に失敗した場合
     */
    private static PKCS10CertificationRequest createCSR(final KeyPair keyPair,
                                                        final String commonName,
                                                        final GeneralNames generalNames) throws GeneralSecurityException {
        final String signatureAlgorithm = "SHA256WithRSAEncryption";
        final X500Principal principal = new X500Principal("CN=" + commonName + ", O=Device Connect Project, L=N/A, ST=N/A, C=JP");
        DERSequence sanExtension= new DERSequence(new ASN1Encodable[] {
                X509Extensions.SubjectAlternativeName,
                new DEROctetString(generalNames)
        });
        DERSet extensions = new DERSet(new DERSequence(sanExtension));
        DERSequence extensionRequest = new DERSequence(new ASN1Encodable[] {
                PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
                extensions
        });
        DERSet attributes = new DERSet(extensionRequest);
        return new PKCS10CertificationRequest(
                signatureAlgorithm,
                principal,
                keyPair.getPublic(),
                attributes,
                keyPair.getPrivate());
    }

    private static class SAN {
        final int mTagNo;
        final String mName;

        SAN(final int tagNo, final String name) {
            mTagNo = tagNo;
            mName = name;
        }
    }
}
