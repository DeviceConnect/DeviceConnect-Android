/*
 CertificateAuthority.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.ssl;


import android.content.Context;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1StreamParser;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.DLSet;
import org.bouncycastle.asn1.DLTaggedObject;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.x500.X500Principal;

/**
 * ローカル認証局.
 *
 * ルート証明書、つまりローカル認証局の自己署名証明書を生成・永続化する.
 * ルート証明書は {@link #getRootCertificate()} で取得できる.
 *
 * 外部からの証明書署名要求を受けた際には、そのルート証明書によって署名した証明書を返す.
 * 証明書署名要求は、{@link #requestCertificate(byte[])} で実行できる.
 *
 * NOTE:
 * 実験的な実装のため、証明書署名要求で指定した情報のうち、
 * Subject Alternative Names (SANs)のみが証明書に反映されることに注意.
 *
 * @author NTT DOCOMO, INC.
 */
class CertificateAuthority {

    /**
     * ロガー.
     */
    private final Logger mLogger = Logger.getLogger("LocalCA");

    /**
     * ルート証明書のキーストア.
     */
    private final RootKeyStoreManager mRootKeyStoreMgr;

    /**
     * ルート証明書の発行者名.
     */
    private final String mIssuerName;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param issuerName ルート証明書の発行者名
     * @param keyStoreFileName キーストアのファイル名
     * @param keyStorePassword キーストアのパスワード
     */
    CertificateAuthority(final Context context,
                         final String issuerName,
                         final String keyStoreFileName,
                         final String keyStorePassword) {
        mRootKeyStoreMgr = new RootKeyStoreManager(context, issuerName, keyStoreFileName, keyStorePassword);
        mIssuerName = issuerName;
    }

    /**
     * ルート証明書を取得する.
     *
     * <p>
     * 確実に証明書を返すために、ルート証明書が未生成だった場合は生成処理実行後に処理を返す.
     * 最大10秒間ブロックする.
     * </p>
     *
     * @return ルート証明書
     */
    byte[] getRootCertificate() {
        final KeyStore[] keyStore = new KeyStore[1];
        final KeyStoreError[] errors = new KeyStoreError[1];
        final CountDownLatch lock = new CountDownLatch(1);

        // キーストア取得
        mRootKeyStoreMgr.requestKeyStore(mIssuerName, new KeyStoreCallback() {
            @Override
            public void onSuccess(final KeyStore result, final Certificate cert, final Certificate rootCert) {
                mLogger.severe("Got Root CA keystore: subject = issuer = " + mIssuerName);
                keyStore[0] = result;
                lock.countDown();
            }
            @Override
            public void onError(final KeyStoreError error) {
                mLogger.severe("Failed to get keystore: " + error);
                errors[0] = error;
                lock.countDown();
            }
        });
        try {
            lock.await(10, TimeUnit.SECONDS);
            if (keyStore[0] == null && errors[0] == null) {
                mLogger.log(Level.SEVERE, "Timeout was occurred for keystore generation.");
                return null;
            }
            if (keyStore[0] != null) {
                Certificate rootCert = keyStore[0].getCertificate(mIssuerName);
                if (rootCert != null) {
                    return rootCert.getEncoded();
                } else {
                    throw new IllegalStateException("Fix bug.");
                }
            }
        } catch (InterruptedException e) {
            mLogger.log(Level.SEVERE, "Failed to encode certificate to byte array.", e);
        } catch (GeneralSecurityException e) {
            mLogger.log(Level.SEVERE, "Failed to get certificate from keystore.", e);
        }
        return null;
    }

    /**
     * 証明書署名要求をもとに証明書を発行する.
     *
     * @param pkcs10 PKCS#10形式の証明書署名要求.
     * @return ルート証明書によって署名された証明書. 発行に失敗した場合はnull
     */
    byte[] requestCertificate(final byte[] pkcs10) {
        try {
            if (getRootCertificate() == null) {
                return null;
            }

            // 証明書要求を解析
            PKCS10CertificationRequest request = new PKCS10CertificationRequest(pkcs10);
            PrivateKey signingKey = mRootKeyStoreMgr.getPrivateKey(mIssuerName);
            SubjectPublicKeyInfo publicKeyInfo = request.getSubjectPublicKeyInfo();
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyInfo.getEncoded()));
            KeyPair keyPair = new KeyPair(publicKey, signingKey);
            X500Principal subject = new X500Principal("CN=localhost");
            X500Principal issuer = new X500Principal("CN=" + mIssuerName);
            GeneralNames generalNames = parseSANs(request);

            // 証明書発行
            Certificate certificate = mRootKeyStoreMgr.generateX509V3Certificate(keyPair, subject, issuer, generalNames, false);
            return certificate.getEncoded();
        } catch (GeneralSecurityException e) {
            mLogger.log(Level.SEVERE, "Failed to generate certificate to byte array.", e);
        } catch (IOException e) {
            mLogger.log(Level.SEVERE, "Failed to parse SANs in certificate.", e);
        }
        return null;
    }

    /**
     * 証明書署名要求から Subject Alternative Names (SANs) を取得する.
     *
     * @param request 証明書署名要求
     * @return SubjectAlternativeNamesを示す {@link GeneralNames} オブジェクト
     * @throws IOException 解析に失敗した場合
     */
    private GeneralNames parseSANs(final PKCS10CertificationRequest request) throws IOException {
        List<GeneralName> generalNames = new ArrayList<>();
        for (Attribute attr : request.getAttributes()) {
            ASN1Primitive extensionRequestObj = attr.toASN1Primitive();
            if (!(extensionRequestObj instanceof DERSequence)) {
                mLogger.info("parseSANs: Not DERSequence");
                continue;
            }
            DERSequence extensionRequest = (DERSequence) extensionRequestObj;
            if (extensionRequest.size() != 2) {
                mLogger.info("parseSANs: size is not 2");
                continue;
            }
            ASN1Encodable idObj = extensionRequest.getObjectAt(0);
            ASN1Encodable contentObj = extensionRequest.getObjectAt(1);
            mLogger.info("parseSANs: idObj = " + idObj.getClass() + ", contentObj = " + contentObj.getClass());
            if (!(idObj instanceof ASN1ObjectIdentifier && contentObj instanceof DLSet)) {
                continue;
            }
            ASN1ObjectIdentifier id = (ASN1ObjectIdentifier) idObj;
            DLSet content = (DLSet) contentObj;
            if (!id.getId().equals("1.2.840.113549.1.9.14")) {
                continue;
            }
            if (content.size() < 1) {
                continue;
            }
            ASN1Encodable extensionsObj = content.getObjectAt(0);
            if (!(extensionsObj instanceof DLSequence)) {
                mLogger.info("parseSANs: extensionsObj is not DERSequence but; " + extensionsObj.getClass());
                continue;
            }
            DLSequence extensions = (DLSequence) extensionsObj;

            for (int k = 0; k < extensions.size(); k++) {
                ASN1Encodable extensionObj = extensions.getObjectAt(k);
                if (!(extensionObj instanceof DLSequence)) {
                    mLogger.info("parseSANs: extensionObj is not DERSequence but; " + extensionsObj.getClass());
                    continue;
                }
                DLSequence extension = (DLSequence) extensionObj;
                if (extension.size() != 2) {
                    continue;
                }
                ASN1Encodable extensionIdObj = extension.getObjectAt(0);
                ASN1Encodable extensionContentObj = extension.getObjectAt(1);
                if (!(extensionIdObj instanceof ASN1ObjectIdentifier)) {
                    continue;
                }
                ASN1ObjectIdentifier extensionId = (ASN1ObjectIdentifier) extensionIdObj;
                if (extensionId.getId().equals("2.5.29.17")) {
                    DEROctetString san = (DEROctetString) extensionContentObj;

                    ASN1StreamParser sanParser = new ASN1StreamParser(san.parser().getOctetStream());
                    ASN1Encodable namesObj = sanParser.readObject().toASN1Primitive();
                    if (namesObj instanceof DLSequence) {
                        DLSequence names = (DLSequence) namesObj;
                        for (int m = 0; m < names.size(); m++) {
                            ASN1Encodable nameObj = names.getObjectAt(m);
                            mLogger.info("parseSANs: nameObj = " + nameObj.getClass());
                            if (nameObj instanceof DLTaggedObject) {
                                DLTaggedObject name = (DLTaggedObject) nameObj;
                                switch (name.getTagNo()) {
                                    case GeneralName.dNSName:
                                        generalNames.add(new GeneralName(GeneralName.dNSName, DERIA5String.getInstance(name, false)));
                                        break;
                                    case GeneralName.iPAddress:
                                        generalNames.add(new GeneralName(GeneralName.iPAddress, DERIA5String.getInstance(name, false)));
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (generalNames.size() > 0) {
            ASN1Sequence seq = new DLSequence(generalNames.toArray(new ASN1Encodable[0]));
            return GeneralNames.getInstance(seq);
        }
        return null;
    }
}
