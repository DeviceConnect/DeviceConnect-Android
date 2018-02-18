package org.deviceconnect.android.ssl;


import android.content.ComponentName;
import android.content.Context;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.util.IPAddress;
import org.bouncycastle.x509.X509Attribute;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.x500.X500Principal;

/**
 * エンドポイント用キーストア管理クラス.
 *
 * Device Connect Managerのローカル認証局に対して証明書要求を送信し、エンドポイント用サーバ証明書を取得する.
 */
public class EndPointKeyStoreManager extends AbstractKeyStoreManager implements KeyStoreManager {

    private static final ComponentName DEFAULT_ROOT_CA = new ComponentName("org.deviceconnect.android.manager",
            "org.deviceconnect.android.manager.ssl.DConnectCertificateAuthorityService");

    private final String mAlias;

    private final ComponentName mRootCA;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final Logger mLogger = Logger.getLogger("LocalCA");

    private final List<String> mIPAddresses = new ArrayList<>();

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
    }

    private boolean hasIPAddress(final String ipAddress) {
        for (String address : mIPAddresses) {
            if (address.equals(ipAddress)) {
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
                try {
                    final String alias = ipAddress;
                    if (hasIPAddress(ipAddress)) {
                        mLogger.info("Certificate is cached for alias: " + alias);
                        callback.onSuccess(mKeyStore);
                    } else {
                        mLogger.info("Generating key pair...");
                        final KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
                        final KeyPair keyPair = keyGenerator.generateKeyPair();
                        mLogger.info("Generated key pair.");
                        mLogger.info("Executing certificate request...");
                        final CertificateAuthorityClient localCA = new CertificateAuthorityClient(mContext, mRootCA);
                        final GeneralName name = new GeneralName(GeneralName.iPAddress, ipAddress);
                        localCA.executeCertificateRequest(createCSR(keyPair, ipAddress, name), new CertificateRequestCallback() {
                            @Override
                            public void onCreate(final Certificate cert) {
                                mLogger.info("Generated server certificate");

                                try {
                                    setCertificate(cert, keyPair.getPrivate());
                                    saveKeyStore();
                                    mLogger.info("Saved server certificate");
                                    callback.onSuccess(mKeyStore);
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

    private String getAlias() {
        return mAlias;
    }

    private void setCertificate(final Certificate cert, final PrivateKey privateKey) throws KeyStoreException {
        Certificate[] chain = {cert};
        mKeyStore.setKeyEntry(getAlias(), privateKey, null, chain);
    }

    private static PKCS10CertificationRequest createCSR(final KeyPair keyPair,
                                                        final String commonName,
                                                        final GeneralName generalName) throws GeneralSecurityException {
        final String signatureAlgorithm = "SHA256WithRSAEncryption";
        final X500Principal principal = new X500Principal("CN=" + commonName);

        final Vector<DERObjectIdentifier> objectIDs = new Vector<>();
        objectIDs.add(X509Extensions.BasicConstraints);
        objectIDs.add(X509Extensions.KeyUsage);
        objectIDs.add(X509Extensions.ExtendedKeyUsage);
        objectIDs.add(X509Extensions.SubjectAlternativeName);
        final Vector<X509Extension> values = new Vector<>();
        values.add(new X509Extension(true, new DEROctetString(new BasicConstraints(/* isCA*/ true ))));
        values.add(new X509Extension(true, new DEROctetString(new KeyUsage(160))));
        values.add(new X509Extension(true, new DEROctetString(new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth))));
        values.add(new X509Extension(false, new DEROctetString(new GeneralNames(new DERSequence(new ASN1Encodable[] {
                new GeneralName(GeneralName.dNSName, "localhost"),
                generalName
        })))));
        final X509Extensions x509Extensions = new X509Extensions(objectIDs, values);
        final X509Attribute x509Attribute = new X509Attribute(
                PKCSObjectIdentifiers.pkcs_9_at_extensionRequest.getId(),
                new DERSet(x509Extensions));

        return new PKCS10CertificationRequest(
                signatureAlgorithm,
                principal,
                keyPair.getPublic(),
                new DERSet(x509Attribute),
                keyPair.getPrivate());
    }
}
