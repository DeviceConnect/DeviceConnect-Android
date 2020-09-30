package org.deviceconnect.android.ssl;

import android.content.ComponentName;
import android.content.Context;
import android.widget.Toast;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.pkcs.CertificationRequest;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.deviceconnect.android.logger.AndroidHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.security.auth.x500.X500Principal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class CertificateAuthorityClientTest {

    @Before
    public void setup() {
        Logger logger = Logger.getLogger("LocalCA");
        AndroidHandler handler = new AndroidHandler(logger.getName());
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
    }

    @Test
    public void testExecuteCertificateRequest() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        ComponentName authorityName = new ComponentName("org.deviceconnect.android.test",
                "org.deviceconnect.android.ssl.TestCertificateAuthorityService");

        final AtomicReference<Certificate> result1 = new AtomicReference<>();
        final AtomicReference<Certificate> result2 = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);

        final List<GeneralName> names = new ArrayList<>();
        names.add(new GeneralName(GeneralName.iPAddress, "0.0.0.0"));
        names.add(new GeneralName(GeneralName.iPAddress, "127.0.0.1"));
        names.add(new GeneralName(GeneralName.dNSName, "localhost"));
        GeneralNames generalNames = GeneralNames.getInstance(new DERSequence(names.toArray(new GeneralName[0])));
        final KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        final KeyPair keyPair = keyGenerator.generateKeyPair();

        CertificationRequest request = createCSR(keyPair, "localhost", generalNames);
        CertificateAuthorityClient client = new CertificateAuthorityClient(context, authorityName);
        client.executeCertificateRequest(request, new CertificateRequestCallback() {
            @Override
            public void onCreate(final Certificate certificate, final Certificate rootCertificate) {
                result1.set(certificate);
                result2.set(rootCertificate);
                latch.countDown();
            }

            @Override
            public void onError() {
                latch.countDown();
            }
        });

        if (latch.getCount() > 0) {
            latch.await(10, TimeUnit.SECONDS);
        }

        Certificate certificate = result1.get();
        Certificate rootCertificate = result2.get();
        assertNotNull(certificate);
        assertNotNull(rootCertificate);

        Certificate[] chain = { certificate, rootCertificate};
        String alias = context.getPackageName();
        String password = "0000";
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, password.toCharArray());
        keyStore.setKeyEntry(alias, keyPair.getPrivate(), null, chain);

        chain = keyStore.getCertificateChain(alias);
        assertNotNull(chain);
        assertEquals(2, chain.length);

        File dir = context.getExternalFilesDir(null);
        File file = new File(dir, "test.cer");
        storeCertificate(file, rootCertificate);
    }

    private static void storeCertificate(final File file,
                                         final Certificate certificate)
            throws IOException, CertificateEncodingException {
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("Failed to create new file: " + file.getAbsolutePath());
            }
        }
        try (OutputStream out = new FileOutputStream(file)) {
            out.write(certificate.getEncoded());
            out.flush();
        }
    }

    private static PKCS10CertificationRequest createCSR(final KeyPair keyPair,
                                                        final String commonName,
                                                        final GeneralNames generalNames) throws GeneralSecurityException, IOException {
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
                keyPair.getPrivate(),
                SecurityUtil.getSecurityProvider());
    }
}
