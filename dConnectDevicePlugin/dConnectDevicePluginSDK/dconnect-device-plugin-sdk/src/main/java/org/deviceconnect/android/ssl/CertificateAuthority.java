package org.deviceconnect.android.ssl;


import android.content.Context;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1SetParser;
import org.bouncycastle.asn1.ASN1StreamParser;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.pkcs.CertificationRequestInfo;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PKCS10CertificationRequest;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
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
                    CertificationRequestInfo requestInfo = request.getCertificationRequestInfo();

                    PrivateKey signingKey = mRootKeyStoreMgr.getPrivateKey(mIssuerName);
                    KeyPair keyPair = new KeyPair(request.getPublicKey(), signingKey);
                    X500Principal subject = new X500Principal("CN=localhost");
                    X500Principal issuer = new X500Principal("CN=" + mIssuerName);
                    GeneralNames generalNames = parseSANs(request);
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

    private GeneralNames parseSANs(final PKCS10CertificationRequest request) throws IOException {
        List<ASN1Encodable> generalNames = new ArrayList<>();

        CertificationRequestInfo info = request.getCertificationRequestInfo();
        ASN1Set attributes = info.getAttributes();
        for (int i = 0; i < attributes.size(); i++) {
            DEREncodable extensionRequestObj = attributes.getObjectAt(i);
            if (!(extensionRequestObj instanceof DERSequence)) {
                continue;
            }
            DERSequence extensionRequest = (DERSequence) extensionRequestObj;
            if (extensionRequest.size() != 2) {
                continue;
            }
            DEREncodable idObj = extensionRequest.getObjectAt(0);
            DEREncodable contentObj = extensionRequest.getObjectAt(1);
            if (!(idObj instanceof ASN1ObjectIdentifier && contentObj instanceof DERSet)) {
                continue;
            }
            ASN1ObjectIdentifier id = (ASN1ObjectIdentifier) idObj;
            DERSet content = (DERSet) contentObj;
            if (!id.getId().equals("1.2.840.113549.1.9.14")) {
                continue;
            }
            if (content.size() < 1) {
                continue;
            }
            DEREncodable extensionsObj = content.getObjectAt(0);
            if (!(extensionsObj instanceof DERSequence)) {
                continue;
            }
            DERSequence extensions = (DERSequence) extensionsObj;
            extension:
            for (int k = 0; k < extensions.size(); k++) {
                DEREncodable extensionObj = extensions.getObjectAt(k);
                if (!(extensionObj instanceof DERSequence)) {
                    continue extension;
                }
                DERSequence extension = (DERSequence) extensionObj;
                if (extension.size() != 2) {
                    continue extension;
                }
                DEREncodable extensionIdObj = extension.getObjectAt(0);
                DEREncodable extensionContentObj = extension.getObjectAt(1);
                if (!(extensionIdObj instanceof ASN1ObjectIdentifier)) {
                    continue extension;
                }
                ASN1ObjectIdentifier extensionId = (ASN1ObjectIdentifier) extensionIdObj;
                if (extensionId.getId().equals("2.5.29.17")) {
                    DEROctetString san = (DEROctetString) extensionContentObj;

                    ASN1StreamParser sanParser = new ASN1StreamParser(san.parser().getOctetStream());
                    DEREncodable namesObj = sanParser.readObject().getDERObject();
                    if (namesObj instanceof DERSequence) {
                        DERSequence names = (DERSequence) namesObj;
                        for (int m = 0; m < names.size(); m++) {
                            DEREncodable nameObj = names.getObjectAt(m);
                            if (nameObj instanceof DERTaggedObject) {
                                DERTaggedObject name = (DERTaggedObject) nameObj;
                                switch (name.getTagNo()) {
                                    case GeneralName.dNSName:
                                        generalNames.add(new GeneralName(GeneralName.dNSName, DERIA5String.getInstance(name, false)));
                                        break;
                                    case GeneralName.iPAddress:
                                        generalNames.add(new GeneralName(GeneralName.iPAddress, DEROctetString.getInstance(name, true)));
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (generalNames.size() > 0) {
            return new GeneralNames(new DERSequence(generalNames.toArray(new ASN1Encodable[generalNames.size()])));
        }
        return null;
    }
}
